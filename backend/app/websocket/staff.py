from fastapi import WebSocket


class ConnectionStaff:

    def __init__(self):
        self.connections = {}

    async def connect(self, staff_id, websocket: WebSocket):
        await websocket.accept()
        self.connections[staff_id] = websocket

    def disconnect(self, staff_id):
        if staff_id in self.connections:
            del self.connections[staff_id]

    async def send_to_staff(self, staff_id, message):
        websocket = self.connections.get(staff_id)

        if websocket:
            await websocket.send_json(message)


websocket_staff = ConnectionStaff()