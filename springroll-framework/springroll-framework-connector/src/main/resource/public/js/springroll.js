/**
 * (An outgoing sample)
 * ASK /chats/80
 * Serial-No: 1
 * Content-Length: 18
 * Content-Type: application/json
 * Content-Class: chat.Hello
 *
 * {content:"Hello!"}
 *
 *
 * (An incoming sample)
 * /chats/80 REPLY
 * Re-Serial-No: 1
 * Content-Length: 18
 * Content-Type: application/json
 * Content-Class: chat.Welcome
 *
 * {content:"Welcome!"}
 */
var SpringRollConnection = function (url, onOpen, onError, onClose) {

    var lastSerialNo= 0;
    var handlers = {};
    var asks = {};

    var serialize = function(method, to, headers, content) {
        var output = "";
        output += method;
        if(to) output += " " + to;
        output += "\r\n";
        for(key in headers) {
            output += key + ": " + headers[key] + "\r\n";
        }
        output += "\r\n";
        output += JSON.stringify(content);
        return output;
    };
    var unserialize = function(input) {
        var frame = { headers: {} };
        var lines = input.split("\r\n");
        var i = 0;
        var line = lines[i];
        var parts = line.split(" ");
        if(parts.length > 0) frame.method = parts[parts.length - 1].trim();
        if(parts.length > 1) frame.from = parts[parts.length - 2].trim();
        while((line = lines[++i]).length > 0) {
            parts = line.split(":");
            frame.headers[parts[0].trim()] = parts[1].trim();
        }
        var content = lines.slice(i).join("\r\n");
        frame.content = JSON.parse(content);
        return frame;
    };

    var webSocket = new WebSocket(url);
    webSocket.onopen = function(event) {
        if (console) console.info("WebSocket connected");
        if (onOpen) onOpen(event);
    };
    webSocket.onerror = function (event) {
        if (console) console.error("WebSocket error", event);
        if (onError) onError(event);
    };
    webSocket.onclose = function (event) {
        if (console) console.info("WebSocket closed");
        if (onClose) onClose(event);
    };
    webSocket.onmessage = function(event) {
        if (console) console.debug("<<", event.data);
        var frame = unserialize(event.data);
        switch(frame.method) {
            case "TELL":
                var contentClass = frame.headers["Content-Class"];
                var handler = handlers[contentClass];
                if(handler) handler(frame.content);
                break;
            case "REPLY":
                var reSerialId = frame.headers["Re-Serial-No"];
                var handler = asks[reSerialId];
                if(handler) handler(frame.content);
                delete asks[reSerialId];
                break;
            case "ERROR":
                var reSerialId = frame.headers["Re-Serial-No"];
                if(console) console.error("ask error", frame);
                delete asks[reSerialId];
                break;
            default:
                if(console) console.error("unrecognized frame", frame);
        }
    };

    return {
        "on": function(type, handler) {
            handlers[type] = handler;
        },
        "tell": function(to, contentClass, content) {
            var packet = serialize("TELL", to, {
                "Serial-No": ++lastSerialNo,
                "Content-Class": contentClass
            }, content);
            if (console) console.debug(">>", packet);
            webSocket.send(packet);
        },
        "ask": function(to, contentClass, content, handler) {
            var serialNo = ++lastSerialNo;
            asks[serialNo] = handler;
            var packet = serialize("ASK", to, {
                "Serial-No": serialNo,
                "Content-Class": contentClass
            }, content);
            if (console) console.debug(">>", packet);
            webSocket.send(packet);
        },
        "close": function() {
            webSocket.close();
        }
    };

};