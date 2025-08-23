import { statusMessage } from "./constant.js";

const handle = (data, socket) => {
    data = data.toString().trim();
    // Catch the separation between the headers and the body
    const [head, body] = data.split("\r\n\r\n", 1);
    // POST /users HTTP/1.1, [host: localhost:3000, content-type: application/json, ...]
    const [metadata, ...headers] = head.split("\r\n");

    const request = { metadata: metadata.split(" "), headers, body };
    // share splited metadata 
    const response = {
        metadata: request.metadata,   
        statusCode: 200,
        headers: {
            "Content-Type": "text/plain",
            "Content-Length": 0,
        },
        body: "",
    };
    
    sendResponse(response, socket);

    socket.end();
}

const sendResponse = (response, socket) => {
    var responseData = "";
    responseData += response.metadata[2] + " " + response.statusCode + " " + statusMessage[response.statusCode] + "\r\n";

    for (var prop in response.headers) {
        responseData += prop + ": " + response.headers[prop] + "\r\n";
    }

    responseData += "\r\n";
    responseData += response.body;

    socket.write(responseData);
}

export default handle;