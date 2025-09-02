package server

import (
	"fmt"
	"net/http"
	"os"
	"strconv"
	"time"

	_ "github.com/joho/godotenv/autoload"
)

type Server struct {
	address string
	port    int
}

func NewServer() *http.Server {
	address := os.Getenv("ADDRESS")
	port, _ := strconv.Atoi(os.Getenv("PORT"))
	NewServer := &Server{
		address: address,
		port:    port,
	}

	// Declare Server config
	server := &http.Server{
		Addr:         fmt.Sprintf("%s:%d", NewServer.address, NewServer.port),
		Handler:      NewServer.RegisterRoutes(),
		IdleTimeout:  time.Minute,
		ReadTimeout:  10 * time.Second,
		WriteTimeout: 30 * time.Second,
	}

	return server
}
