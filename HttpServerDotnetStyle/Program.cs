using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;

class Program
{
    public static void Main()
    {
        // Set the TcpListener on port 13000.
        Int32 port = 8080;
        IPAddress ip = IPAddress.Parse("127.0.0.1");

        StartServer(ip, port);

        Console.WriteLine("\nHit enter to continue...");
        Console.Read();
    }

    public static void StartServer(IPAddress ip, Int32 port)
    {
        TcpListener server = null;
        List<TcpClient> clients = new List<TcpClient>();
        
        try
        {
            server = new TcpListener(ip, port);

            // Start listening for client requests.
            server.Start();

            // Enter the listening loop.
            while (true)
            {
                Console.Write("Waiting for a connection... ");

                // Perform a blocking call to accept requests.
                // You could also use server.AcceptSocket() here.
                TcpClient client = server.AcceptTcpClient();
                clients.Add(client);

                //Create a thread to receive and send message
                Thread thread = new Thread(() => ProcessMessage(client, clients));
                thread.Start();
            }
        }
        catch (SocketException e)
        {
            Console.WriteLine("SocketException: {0}", e);
        }
        finally
        {
            server.Stop();
        }
    }

    public static async void ProcessMessage(TcpClient client, List<TcpClient> clientList)
    {
        Console.WriteLine("Connected!");

        try
        {
            // Buffer for reading data
            Byte[] bytes = new Byte[256];
            int i;

            // Get a stream object for reading and writing
            NetworkStream stream = client.GetStream();


            // Loop to receive all the data sent by the client.
            while ((i = stream.Read(bytes, 0, bytes.Length)) != 0)
            {
                // Translate data bytes to a ASCII string.
                var data = Encoding.ASCII.GetString(bytes, 0, i);
                Console.WriteLine("Received: {0}", data);

                // TODO: Check for length later
                string res = data;

                byte[] msg = Encoding.ASCII.GetBytes(res);

                // Send back a response.
                foreach (var connectingClient in clientList)
                {
                    //if (connectingClient == client) continue;

                    NetworkStream clientStream = client.GetStream();
                    clientStream.Write(msg, 0, msg.Length);
                    Console.WriteLine("Sent: {0}", data);
                }
            }
        }
        catch (Exception e)
        {
            Console.WriteLine("Socket err:{0}", e.Message);
            clientList.Remove(client);
        }
    }

}