import msgpack from 'msgpack-lite';

const WEBSOCKET_URL = 'ws://localhost:20506/ws';

const ws = new WebSocket(WEBSOCKET_URL);

ws.addEventListener('open', lis => {
    console.log("Connected!")
    sendLinks(getDownloadLinks());
});
ws.addEventListener('message', lis => {
    console.log(lis.data);
})
ws.addEventListener('error', lis => {
    console.error("Server Is Offline!");
})

function sendLinks(list) {
    const encoded = msgpack.encode(list);
    ws.send(encoded);
}
function getDownloadLinks() {
    return [
//        [1, "https://dl2.soft98.ir/soft/n/Notepad.8.9.6.2.x86.rar?1780532561"],
        [2, "https://dl2.soft98.ir/soft/t/TweakNow.WinSecret.Plus.9.4.7.rar?1780532577"],
//        [3, "https://dl2.soft98.ir/soft/t/TweakNow.WinSecret.Plus.v8.0.1.x64.Portable.rar?1780532585"],
//        [4, "https://dl2.soft98.ir/soft/t/TweakNow.WinSecret.4.2.7.rar?1780532600"]
    ];
}