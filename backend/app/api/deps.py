from uuid import UUID

import jwt
from fastapi import Depends, HTTPException ,status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

from backend.core.config import SessionLocal
from backend.core.security import decode_token
from backend.repositories.account_repository import AccountRepository
from backend.services.account_service import AccountService

security = HTTPBearer()

async def get_db():
    async with SessionLocal() as db:
        yield db

def get_account_service(
    db=Depends(get_db)
):
    repository = AccountRepository(db)

    return AccountService(repository)

def get_current_account(
    credentials: HTTPAuthorizationCredentials = Depends(security)
) :
    try:
        token = credentials.credentials

        payload = decode_token(token)

        if payload.get("type") != "access_token":
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Access token required"
            )

        user_id = payload.get("sub")

        if user_id is None:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token does not contain user identifier"
            )

        return UUID(user_id)

    except jwt.ExpiredSignatureError:
        raise HTTPException(
            status_code=401,
            detail="Token đã hết hạn. Hãy dùng refresh token!"
        )

    except jwt.InvalidTokenError:
        raise HTTPException(
            status_code=401,
            detail="Token không hợp lệ hoặc bị làm giả!"
        )