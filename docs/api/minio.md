# Dokumentasi API — File Storage (MinIO)

Base path: `/minio`

---

## GET /minio/upload-url

**Autentikasi**: Publik

Menghasilkan presigned URL untuk mengunggah file langsung ke MinIO object storage. URL yang dihasilkan bersifat sementara dan hanya berlaku dalam kurun waktu tertentu.

### Query Parameters

| Parameter | Tipe   | Wajib | Keterangan                                               |
|-----------|--------|-------|----------------------------------------------------------|
| extension | string | Ya    | Ekstensi file yang akan diunggah, contoh: `pdf`, `jpg`, `png` |

### Contoh Request

```
GET /minio/upload-url?extension=pdf
```

### Contoh Response

```json
{
  "success": true,
  "message": "Successfully generated upload URL",
  "result": {
    "uploadUrl": "https://storage.example.com/bucket/objects/a1b2c3d4-uuid.pdf?X-Amz-Signature=...",
    "objectKey": "a1b2c3d4-uuid.pdf",
    "expiresAt": "2026-04-02T10:15:00"
  }
}
```

> **Catatan penggunaan**: Setelah mendapatkan `uploadUrl`, lakukan HTTP `PUT` langsung ke URL tersebut dengan isi file sebagai request body. Setelah berhasil, gunakan `objectKey` atau URL lengkap file tersebut saat mengisi field seperti `certificateUrl` pada endpoint lain.
