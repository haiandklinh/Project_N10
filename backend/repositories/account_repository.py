from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.exc import IntegrityError
from backend.core.security import create_token, hash_token, get_password_hash
from backend.models.account import Account
from backend.models.refresh_token import RefreshToken


class AccountRepository :
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_account_by_phone(self, phone_number):
        result = await self.db.execute(
            select(Account)
            .where(Account.phone_number == phone_number)
        )

        return result.scalar_one_or_none()

    async def create_refresh_token(self, account_id):
        token = create_token(account_id,"refresh_token")
        refresh_token = RefreshToken(
            token=hash_token(token),
            account_id=account_id,
        )
        self.db.add(refresh_token)

        await self.db.commit()
        await self.db.refresh(refresh_token)

    async def create_account(self,phone_number , password, role):
        new_account = Account(
            phone_number = phone_number ,
            password = get_password_hash(password) ,
            role = role  ,
            state = 'ok'
        )
        try:
            self.db.add(new_account)
            await self.db.commit()
            await self.db.refresh(new_account)
            return new_account

        except IntegrityError:
            await self.db.rollback()
            return None
    async def update_account(self,phone_number,password):
        account = await self.get_account_by_phone(phone_number)

        if account is None:
            return None

        account.password = get_password_hash(password)

        await self.db.commit()
        await self.db.refresh(account)

        return account