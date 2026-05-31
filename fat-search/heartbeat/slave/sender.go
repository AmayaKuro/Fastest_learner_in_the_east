package main

import (
	"log"
	"net"
	"time"
)

type Sender struct {
	port   string
	nodeID string
}

func NewSender(port, nodeID string) *Sender {
	return &Sender{
		port:   port,
		nodeID: nodeID,
	}
}

func (s *Sender) Start() error {
	addr, err := net.ResolveUDPAddr("udp", s.port)
	if err != nil {
		return err
	}

	conn, err := net.DialUDP("udp", nil, addr)
	if err != nil {
		return err
	}

	log.Printf("Sending heartbeats on UDP %s\n", s.port)

	// Start goroutine to send messages
	s.sendHeartbeats(conn)

	return nil
}

func (s *Sender) sendHeartbeats(conn *net.UDPConn) {
	defer conn.Close()

	ticker := time.NewTicker(2 * time.Second)

	for range ticker.C {
		message := []byte(s.nodeID)
		conn.Write(message)
	}
}
