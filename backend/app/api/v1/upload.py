from fastapi import APIRouter, UploadFile, File, HTTPException
from core.cloudinary_custom import cloud_service

router = APIRouter(prefix="/upload", tags=["Upload"])

@router.post("", summary="Upload image to Cloudinary")
async def upload_image(file: UploadFile = File(...)):
    try:
        if not file.content_type or not file.content_type.startswith("image/"):
            raise HTTPException(status_code=400, detail="File phải là định dạng hình ảnh")

        result = cloud_service.upload_image(file)
        return {
            "success": True,
            "url": result["url"],
            "public_id": result["public_id"]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Lỗi upload ảnh: {str(e)}")
