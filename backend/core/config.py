from pydantic_settings import BaseSettings
from sqlalchemy.ext.asyncio import AsyncSession ,async_sessionmaker
from sqlalchemy.ext.asyncio import create_async_engine



class Settings(BaseSettings):
    # Database
    DATABASE_URL :str
    DB_ECHO: bool
    DB_SSL: bool = False


    # Redis
    REDIS_URL: str

    # JWT
    JWT_SECRET: str
    JWT_ALGORITHM: str
    ACCESS_TOKEN_EXPIRE_MINUTES: int
    REFRESH_TOKEN_EXPIRE_DAYS: int

    #Cloudinary
    CLOUDINARY_CLOUD_NAME: str
    CLOUDINARY_API_KEY: str
    CLOUDINARY_API_SECRET: str
    # Type Notification for manager
    CHANGE_WORK_SHIFT_REQUEST:str
    CHANGE_LEAVE_REQUEST :str
    ATTENDANCE_ADJUSTMENT_REQUEST :str
    JOIN_COMPANY_REQUEST :str

    # Type Notification for Staff
    CHANGE_WORK_SHIFT_RESPONSE :str
    CHANGE_LEAVE_RESPONSE :str
    ATTENDANCE_ADJUSTMENT_RESPONSE :str
    JOIN_COMPANY_RESPONSE :str
    class Config:
        env_file = ".env"
        case_sensitive = True # phân biệt hoa và thường

settings = Settings()
engine = create_async_engine(
    settings.DATABASE_URL,
    echo=settings.DB_ECHO,
    connect_args={"ssl": True} if settings.DB_SSL else {},
)
SessionLocal = async_sessionmaker(
    bind=engine,
    class_=AsyncSession,
    expire_on_commit=False,
)



