from fastapi import WebSocket, WebSocketDisconnect, APIRouter, Depends

from app.api.deps import get_db, security
from app.websocket.manager import websocket_manager
from app.websocket.staff import websocket_staff
from core.security import verify_token
from repositories.manager_repository import ManagerRepository
from repositories.staff_repository import StaffRepository

router = APIRouter(
    tags=["Websocket"]
)


@router.websocket("/ws/manager/notifications")
async def notification_websocket(
    websocket: WebSocket,
    db=Depends(get_db)
):
    credentials = await security(websocket)


    account_id = verify_token(
        credentials.credentials
    )


    manager_repo = ManagerRepository(db)

    manager = await manager_repo.get_profile_by_id_account(
        account_id
    )


    if manager is None:
        await websocket.close(code=1008)
        return

    manager_id = manager.id


    await websocket.accept()


    websocket_manager.connections[manager_id] = websocket

    try:
        while True:
            await websocket.receive_text()

    except WebSocketDisconnect:
        websocket_manager.disconnect(manager_id)


@router.websocket("/ws/staff/notifications")
async def notification_websocket(
    websocket: WebSocket,
    db=Depends(get_db)
):
    credentials = await security(websocket)


    account_id = verify_token(
        credentials.credentials
    )


    staff_repo = StaffRepository(db)

    staff = await staff_repo.get_profile_by_id_account(
        account_id
    )


    if staff is None:
        await websocket.close(code=1008)
        return

    staff_id = staff.id


    await websocket.accept()


    websocket_staff.connections[staff_id] = websocket

    try:
        while True:
            await websocket.receive_text()

    except WebSocketDisconnect:
        websocket_manager.disconnect(staff_id)
