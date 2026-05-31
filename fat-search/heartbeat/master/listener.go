package main

import (
	"log"
	"net"
	"sync"
	"time"
)

type NodeStatus struct {
	NodeID   string
	LastSeen time.Time
	IsOnline bool
}

type Listener struct {
	port       string
	nodes      map[string]*NodeStatus
	nodesMutex sync.RWMutex
}

func NewListener(port string) *Listener {
	return &Listener{
		port:  port,
		nodes: make(map[string]*NodeStatus),
	}
}

func (l *Listener) Start() error {
	addr, err := net.ResolveUDPAddr("udp", l.port)
	if err != nil {
		return err
	}

	conn, err := net.ListenUDP("udp", addr)
	if err != nil {
		return err
	}

	log.Printf("Listening for heartbeats on UDP %s\n", l.port)
	// Start goroutine to receive messages
	go l.checkNodesBackgroundJob()
	l.listen(conn)
	// Start goroutine for the background job to check offline nodes

	return nil
}

func (l *Listener) listen(conn *net.UDPConn) {
	defer conn.Close()
	buffer := make([]byte, 1024)

	for {
		log.Printf("come here!\n")
		_, _, err := conn.ReadFromUDP(buffer)
		if err != nil {
			log.Printf("Error reading from UDP: %v\n", err)
			continue
		}

		log.Printf("come here2!\n")

		// Save status using node's address as a simple NodeID
		data := string(buffer)
		l.updateNodeStatus(data)
	}
}

func (l *Listener) updateNodeStatus(nodeID string) {
	l.nodesMutex.Lock()
	defer l.nodesMutex.Unlock()

	if _, exists := l.nodes[nodeID]; !exists {
		log.Printf("New node connected: %s\n", nodeID)
		l.nodes[nodeID] = &NodeStatus{
			NodeID:   nodeID,
			IsOnline: true,
		}
	}

	// Update lastSeen timestamp
	l.nodes[nodeID].LastSeen = time.Now()

	if !l.nodes[nodeID].IsOnline {
		log.Printf("Node %s is back online\n", nodeID)
		l.nodes[nodeID].IsOnline = true
	}
}

// Background job to check which node lastSeen is higher than 3 seconds
func (l *Listener) checkNodesBackgroundJob() {
	ticker := time.NewTicker(1 * time.Second)
	defer ticker.Stop()

	for range ticker.C {
		l.nodesMutex.Lock()
		for id, status := range l.nodes {
			if status.IsOnline && time.Since(status.LastSeen) > 3*time.Second {
				status.IsOnline = false
				log.Printf("Node %s is offline (last seen > 3s ago)\n", id)
			}
		}
		l.nodesMutex.Unlock()
	}
}
