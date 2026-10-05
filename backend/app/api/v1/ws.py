from fastapi import WebSocket, WebSocketDisconnect, APIRouter, Depends

from app.api.deps import get_db, security
from app.websocket.manager import websocket_manager
from core.security import verify_token
from repositories.manager_repository import ManagerRepository

router = APIRouter(
    prefix="/ws",
    tags=["Websocket"]
)


@router.websocket("/manager/notifications")
async def notification_websocket(
    websocket: WebSocket ,
    db= Depends(get_db)
):
    credentials = await security(websocket)

    account_id = verify_token(
        credentials.credentials
    )

    if account_id is None:
        await websocket.close(code=1008)
        return

    manager_repo = ManagerRepository(db)
    manager =await  manager_repo.get_profile_by_id_account(account_id)

    if manager is None:
        await websocket.close(code=1008)
        return

    manager_id = manager.id_manager

    # =========================
    # 6. Lưu connection
    # =========================

    await websocket_manager.connect(
        manager_id,
        websocket
    )

    try:
        while True:
            await websocket.receive_text()

    except WebSocketDisconnect:

        websocket_manager.disconnect(
            manager_id
        )


