# Minio Controller Documentation

Base URL: `/minio`

---

## 1. Get Upload URL

**Endpoint:** `GET /minio/upload-url`

**Access:** Public

**Deskripsi:** Menghasilkan Presigned URL untuk upload file ke Minio storage. URL ini dapat digunakan untuk direct upload dari client tanpa perlu server relay.

### Query Parameters

| Parameter | Type | Required | Deskripsi |
|-----------|------|----------|-----------|
| extension | String | Ya | Ekstension file (pdf, jpg, png, doc, dll) |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully generated upload URL",
  "result": {
    "uploadUrl": "https://minio.example.com/bucket/uploads/file-uuid.pdf?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...",
    "fileId": "550e8400-e29b-41d4-a716-446655440000",
    "bucket": "uploads",
    "expiresIn": 3600
  }
}
```

### Result Fields

| Field | Type | Deskripsi |
|-------|------|-----------|
| uploadUrl | String | URL presigned untuk upload file |
| fileId | String | UUID unik untuk file yang akan diupload |
| bucket | String | Bucket Minio tempat file disimpan |
| expiresIn | Integer | Waktu berlaku URL dalam detik (default 1 jam) |

### Error Response - Missing Extension (400 Bad Request)

```json
{
  "success": false,
  "message": "extension parameter is required"
}
```

### Error Response - Invalid Extension (400 Bad Request)

```json
{
  "success": false,
  "message": "File extension not allowed"
}
```

### Error Response - Minio Connection Error (500 Internal Server Error)

```json
{
  "success": false,
  "message": "Failed to generate upload URL"
}
```

---

## Supported File Types

| Extension | MIME Type | Deskripsi |
|-----------|-----------|-----------|
| pdf | application/pdf | PDF Documents |
| jpg | image/jpeg | JPEG Images |
| jpeg | image/jpeg | JPEG Images |
| png | image/png | PNG Images |
| gif | image/gif | GIF Images |
| webp | image/webp | WebP Images |
| doc | application/msword | Word Documents |
| docx | application/vnd.openxmlformats-officedocument.wordprocessingml.document | Word Documents |
| xls | application/vnd.ms-excel | Excel Spreadsheets |
| xlsx | application/vnd.openxmlformats-officedocument.spreadsheetml.sheet | Excel Spreadsheets |

---

## Upload Process

### Client-Side Implementation

1. **Get Upload URL:**
   ```javascript
   const response = await fetch('/minio/upload-url?extension=pdf');
   const { uploadUrl, fileId } = await response.json().result;
   ```

2. **Upload File to Presigned URL:**
   ```javascript
   const file = new File(['content'], 'document.pdf');
   await fetch(uploadUrl, {
     method: 'PUT',
     headers: {
       'Content-Type': 'application/pdf'
     },
     body: file
   });
   ```

3. **Store File ID:**
   - Simpan `fileId` untuk referensi file di database

---

## Response Format

Endpoint mengembalikan `WebResponse` dengan struktur:

```json
{
  "success": boolean,
  "message": string,
  "result": object
}
```

## Authentication

- **Get Upload URL:** Public (no authentication required)

## Security Notes

- Presigned URL hanya berlaku untuk waktu tertentu (default 1 jam)
- URL dikunci untuk specific file extension dan bucket
- Setiap upload mendapat unique file ID untuk tracking
- Server mencatat semua upload attempts untuk audit

## Best Practices

- Validate file size pada client-side sebelum generate upload URL
- Gunakan file ID yang diterima untuk link storage di database
- Handle URL expiration dengan retry logic
- Implementasikan progress tracking pada client untuk UX yang baik
