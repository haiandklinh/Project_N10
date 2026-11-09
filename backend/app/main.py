import os
from secrets import compare_digest

from fastapi import FastAPI, HTTPException
from sqlalchemy import text

from app.api.router import api_router
from core.config import engine

app = FastAPI()

app.include_router(api_router)

KEEPALIVE_TOKEN = os.getenv("KEEPALIVE_TOKEN", "")


@app.get("/")
async def health_check():
    return {
        "status": "ok",
        "service": "attendance-api"
    }


@app.get("/_internal/db-ping/{token}", include_in_schema=False)
async def database_keepalive(token: str):
    # Chỉ cho phép monitor có token hợp lệ truy cập
    if not KEEPALIVE_TOKEN or not compare_digest(
        token, KEEPALIVE_TOKEN
    ):
        raise HTTPException(
            status_code=404,
            detail="Not found"
        )

    try:
        # Truy vấn database để duy trì kết nối
        async with engine.connect() as conn:
            await conn.execute(text("SELECT 1"))

        return {
            "status": "ok",
            "database": "reachable"
        }

    except Exception as exc:
        raise HTTPException(
            status_code=503,
            detail="Database unavailable"
        ) from exc