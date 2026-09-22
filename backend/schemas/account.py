from pydantic import BaseModel



class CheckPhoneRequest(BaseModel) :
    phone_number :str
class CheckPhoneResponse(BaseModel) :
    exist : bool

class LoginResponse(BaseModel) :
    access_token :str
    refresh_token : str

class LoginRequest(BaseModel) :
    phone_number : str
    password : str

class SendOTPRequest(BaseModel) :
    phone_number :str

class SendOTPResponse(BaseModel) :
    OTP:str

class VerifyOTPResponse(BaseModel) :
    is_verified :bool
class VerifyOTPRequest(BaseModel) :
    phone_number :str
    OTP :str
class MessageResponse(BaseModel) :
    message :str

class RegisterRequest(BaseModel) :
    phone_number :str
    password :str

class ResetPasswordRequest(BaseModel) :
    phone_number :str
    password :str

class RefreshResponse(BaseModel) :
    access_token :str

class RefreshRequest(BaseModel) :
    refresh_token :str