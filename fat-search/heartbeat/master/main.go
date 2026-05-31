package main

import (
	"fmt"
	"log"
)

func main() {
	fmt.Println("Go is up and running!")
	listener := NewListener(":6969")
	if err := listener.Start(); err != nil {
		log.Fatalf("Error starting listener: %v", err)
	}
}
