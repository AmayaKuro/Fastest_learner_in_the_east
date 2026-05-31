package main

import "fmt"

func main() {
	fmt.Println("Go is up and running!")
	sender := NewSender(":6969", "C36")
	if err := sender.Start(); err != nil {
		fmt.Printf("Error starting sender: %v\n", err)
	}
}
