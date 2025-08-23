import net from 'net';
import handle from './handler.js';

const PORT = 8080;
const HOST = '127.0.0.1'; // localhost

// Create the server
const server = net.createServer((socket) => {
  console.log('New client connected:', socket.remoteAddress, socket.remotePort);
  
  // Handle incoming data from client
  socket.on('data', (data) => {
    const message = data.toString().trim();
    console.log(`Client says: ${message}`);
    handle(data, socket);
  });

  // Handle client disconnect
  socket.on('end', () => {
    console.log('Client disconnected');
  });

  // Handle errors
  socket.on('error', (err) => {
    console.error('Socket error:', err.message);
  });
});

// Start listening
server.listen(PORT, HOST, () => {
  console.log(`TCP Server listening on http://${HOST}:${PORT}`);
});

// Handle server errors
server.on('error', (err) => {
  console.error('Server error:', err.message);
});