import msgpack from 'msgpack-lite';

const WEBSOCKET_URL = 'ws://127.0.0.1:20506/ws';
let ws = null;
let retryCount = 0;
let isConnecting = false;
const MAX_RETRIES = 50;
const RETRY_DELAY = 5 * 1000 * 60;

chrome.runtime.onInstalled.addListener(() => {
    chrome.contextMenus.create({
        id: "send-for-jvdm",
        title: "Connecting to server...",
        contexts: ["link", "video", "audio"]
    });
    connectWebSocket();
});

chrome.contextMenus.onClicked.addListener((info, tab) => {
    if (info.menuItemId === "send-for-jvdm") {
        if (ws && ws.readyState === WebSocket.OPEN) {
            const downloadUrl = info.linkUrl || info.srcUrl;
            if (downloadUrl) {
                triggerDownload(downloadUrl);
            }
        } else {
            retryCount = 0;
            if (!isConnecting) {
                connectWebSocket();
            }
        }
    }
});

function triggerDownload(url) {
    sendLinks([
        [0, url]
    ]);
}

function updateContextMenu(title) {
    chrome.contextMenus.update("send-for-jvdm", { title: title });
}

function connectWebSocket() {
    isConnecting = true;
    updateContextMenu("Connecting");

    ws = new WebSocket(WEBSOCKET_URL);

    ws.addEventListener('open', lis => {
        console.log("Connected!");
        retryCount = 0;
        isConnecting = false;
        updateContextMenu("Force JVDM to download");
    });

    ws.addEventListener('message', lis => {
        console.log(lis.data);
    });

    ws.addEventListener('error', lis => {
        console.error("Server Is Offline!");
    });

    ws.addEventListener('close', lis => {
        isConnecting = false;
        if (retryCount < MAX_RETRIES) {
            updateContextMenu(`Retry connection (${retryCount + 1}/${MAX_RETRIES})`);
            retryCount++;
            setTimeout(() => {
                if (!isConnecting && (!ws || ws.readyState !== WebSocket.OPEN)) {
                    connectWebSocket();
                }
            }, RETRY_DELAY);
        } else {
            updateContextMenu("Connection failed. Click to retry connection");
        }
    });
}

function sendLinks(list) {
    if (ws && ws.readyState === WebSocket.OPEN) {
        const encoded = msgpack.encode(list);
        ws.send(encoded);
    }
}