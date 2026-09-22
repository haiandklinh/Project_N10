import secrets

from fastapi import HTTPException
from jwt import ExpiredSignatureError, InvalidTokenError

from backend.core.redis_custom import redis_client
from backend.core.security import verify_password, create_token, decode_token
from backend.repositories.account_repository import AccountRepository
from backend.schemas.account import CheckPhoneRequest, CheckPhoneResponse, LoginRequest, LoginResponse, SendOTPRequest, \
    SendOTPResponse, VerifyOTPRequest, VerifyOTPResponse, RegisterRequest, MessageResponse, ResetPasswordRequest, \
    RefreshRequest, RefreshResponse


class AccountService:

    def __init__(self, repository: AccountRepository):
        self.repository = repository

    async def check_phone(self, request:CheckPhoneRequest) -> CheckPhoneResponse:
        account = await self.repository.get_account_by_phone(
            request.phone_number
        )

        return CheckPhoneResponse(
            exist=account is not None
        )

    async def login(self,request:LoginRequest) ->LoginResponse:
        account = await self.repository.get_account_by_phone(
            request.phone_number
        )

        if account is None:
            raise HTTPException(
                status_code=401,
                detail="Invalid phone number or password"
            )

        if not verify_password(request.password, account.password):
            raise HTTPException(
                status_code=401,
                detail="Invalid password"
            )

        access_token =create_token(account.id,"access_token")
        refresh_token =await self.repository.create_refresh_token(account.id)
        return LoginResponse(
            access_token=access_token,
            refresh_token=refresh_token
        )
    async def send_otp(self,request : SendOTPRequest) -> SendOTPResponse:
        otp =  str(secrets.randbelow(1000000)).zfill(6)
        redis_client.set(request.phone_number,otp,180)
        return SendOTPResponse(
            OTP = otp
        )
    async def verify_otp(self,request:VerifyOTPRequest)->VerifyOTPResponse:
        otp = redis_client.get(request.phone_number)
        if request.OTP == otp :
            return VerifyOTPResponse(
                is_verified= True
            )
        return VerifyOTPResponse(
            is_verified= False
        )
    async def register(self,request: RegisterRequest) -> MessageResponse:
        new_account = self.repository.create_account(request.phone_number,request.password,request.role)
        if new_account is None:
            return MessageResponse(
                message= "Register Fail"
            )
        return MessageResponse(
            message="Register Success"
        )

    async def reset_password(self,request:ResetPasswordRequest)->MessageResponse:
        account = self.repository.update_account(request.phone_number,request.password)
        if account is  None :
            return MessageResponse(
                message= "Reset Fail"
            )
        return MessageResponse(
            message="Reset Success"
        )

    async def refresh_token(self,request:RefreshRequest)->RefreshResponse:
        try:
            payload = decode_token(request.refresh_token)
            if payload.get('type') != "refresh_token":
                raise HTTPException(401, "Refresh token required")
            account_id = payload.get('sub')
            if account_id is None:
                raise HTTPException(401, "Invalid refresh token")
            new_access_token = create_token(account_id, "access_token")
            return RefreshResponse(
                access_token = new_access_token ,
            )
        except ExpiredSignatureError:
            raise HTTPException(
                status_code=401,
                detail="Refresh token expired"
            )
        except InvalidTokenError:
            raise HTTPException(
                status_code=401,
                detail="Invalid refresh token"
            )