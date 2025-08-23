using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace HttpServerDotnetStyle
{
    class Client
    {
        public static void Main()
        {
            // Set the TcpListener on port 13000.
            Int32 port = 8080;
            IPAddress ip = IPAddress.Parse("127.0.0.1");
            List<TcpClient> clients = new List<TcpClient>();
            TcpClient client = new TcpClient(ip.ToString(), port);
            var stream = client.GetStream();

            //for (int i = 0; i < 10; i++)
            //{
            //    clients.Add(new TcpClient(ip.ToString(), port));
            //}
            Thread listenThread = new Thread(() =>
            {
                try
                {
                    Console.WriteLine("Listening to server...");

                    Byte[] bytes = new Byte[256];
                    String data = null;
                    int byteCount = 0;
                    
                    //while (true)
                    //{
                        while ((byteCount = stream.Read(bytes, 0, bytes.Length)) != 0)
                        {
                            data = Encoding.ASCII.GetString(bytes, 0, byteCount);
                            Console.WriteLine($"Received: {data}");
                        //}
                    }
                }
                catch (Exception e)
                {
                    Console.WriteLine(e.Message);
                }
            });
            listenThread.Start();


            Thread thread = new Thread(() =>
            {
                //TcpClient client = new TcpClient(ip.ToString(), port);
                //var writeStream = client.GetStream();
                while (true)
                {
                    Console.Write("Write a message: ");

                    string mes = Console.ReadLine();
                    Byte[] bytes = new Byte[256];

                    bytes = Encoding.ASCII.GetBytes(mes);

                    stream.Write(bytes);
                }
            });

            thread.Start();
        }
        private static async void TIMETOLISTENMFS(List<TcpClient> clients)
        {
            foreach (var client in clients)
            {

                var thread = new Thread(() =>
                {
                    try
                    {
                        Console.WriteLine("Listening to server...");

                        var stream = client.GetStream();
                        Byte[] bytes = new Byte[256];
                        String data = null;
                        int byteCount = 0;
                        Thread.Sleep(1000);
                        while (!stream.CanRead) Thread.Sleep(1000);

                        while ((byteCount = stream.Read(bytes, 0, bytes.Length)) != 0)
                        {
                            data = Encoding.ASCII.GetString(bytes, 0, byteCount);
                            Console.WriteLine($"Received: {data}");
                        }
                    }
                    catch (Exception e)
                    {
                        Console.WriteLine(e.Message);
                    }
                });

                thread.Start();
            }
        }
    }
}
