from datetime import datetime, timezone, timedelta
import hashlib
import jwt
from passlib.context import CryptContext

from core.config import settings

pwd_context = CryptContext(
    schemes=["bcrypt"],
    deprecated="auto"
)
def verify_password(plain_password, hashed_password) -> bool:
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password: str) -> str:
    return pwd_context.hash(password)


def create_token(data:str, token_type:str):
    exp =settings.REFRESH_TOKEN_EXPIRE_DAYS
    if token_type=="access_token":
        exp =settings.ACCESS_TOKEN_EXPIRE_MINUTES
        payload = {
            "sub": data,
            "type": token_type,
            "exp": datetime.now(timezone.utc) +  timedelta(minutes=exp)
        }
    else:
        payload = {
            "sub": data,
            "type": token_type,
            "exp": datetime.now(timezone.utc) +  timedelta(days=exp)
        }
    return jwt.encode(payload, settings.JWT_SECRET, algorithm=settings.JWT_ALGORITHM)

def decode_token(token: str):
    return jwt.decode(token, settings.JWT_SECRET, algorithms=[settings.JWT_ALGORITHM])

def hash_token(token: str):
    return hashlib.sha256(token.encode()).hexdigest()

