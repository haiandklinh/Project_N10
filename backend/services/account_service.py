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

    async def reset_password(self, request: ResetPasswordRequest) -> MessageResponse:
        account = await self.repository.update_account(request.phone_number, request.password)
        if account is None:
            return MessageResponse(
                message="Reset Fail"
            )
        await self.repository.revoke_all_refresh_tokens(account.id)
        return MessageResponse(
            message="Reset Success"
        )

    async def refresh_token(
            self,
            request: RefreshRequest
    ) -> RefreshResponse:

        try:
            # 1. Decode và kiểm tra JWT
            payload = decode_token(request.refresh_token)

            # 2. Đây phải là refresh token
            if payload.get("type") != "refresh_token":
                raise HTTPException(
                    status_code=401,
                    detail="Refresh token required"
                )

            # 3. Lấy account_id
            account_id = payload.get("sub")

            if account_id is None:
                raise HTTPException(
                    status_code=401,
                    detail="Invalid refresh token"
                )

            # 4. Kiểm tra refresh token trong DB
            result = await self.repository.get_rf_token_by_token(
                request.refresh_token
            )

            if result is None:
                raise HTTPException(
                    status_code=401,
                    detail="Refresh token not found"
                )

            # 5. Kiểm tra token đã bị revoke chưa
            if result.is_revoked:
                raise HTTPException(
                    status_code=401,
                    detail="Refresh token has been revoked"
                )

            # 6. Tạo access token mới
            new_access_token = create_token(
                account_id,
                "access_token"
            )

            return RefreshResponse(
                access_token=new_access_token
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

    async def logout(self, request: RefreshRequest) -> MessageResponse:
        await self.repository.revoked_refresh_token(request.refresh_token)
        MessageResponse(
            message="Logout Success"
        )