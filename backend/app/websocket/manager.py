from fastapi import WebSocket


class ConnectionManager:

    def __init__(self):
        self.connections = {}

    async def connect(self, manager_id, websocket: WebSocket):
        await websocket.accept()
        self.connections[manager_id] = websocket

    def disconnect(self, manager_id):
        if manager_id in self.connections:
            del self.connections[manager_id]

    async def send_to_manager(self, manager_id, message):
        websocket = self.connections.get(manager_id)

        if websocket:
            await websocket.send_json(message)


websocket_manager = ConnectionManager()