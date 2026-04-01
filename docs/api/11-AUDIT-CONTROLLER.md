# Audit Controller Documentation

Base URL: `/audit`

---

## 1. Get Audit Logs

**Endpoint:** `GET /audit`

**Access:** Authenticated users (`isAuthenticated()`)

**Deskripsi:** Mengambil log audit untuk entitas tertentu berdasarkan ID. Hanya pengguna yang memiliki akses dapat melihat log entity-nya.

### Query Parameters

| Parameter | Type | Required | Deskripsi |
|-----------|------|----------|-----------|
| entity | String | Ya | Tipe entitas (User, Company, Review, Certificate, dll) |
| id | Long | Ya | ID dari entitas |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Audit log fetched",
  "result": [
    {
      "id": 1,
      "entity": "User",
      "entityId": 5,
      "action": "CREATE",
      "actionBy": "admin@example.com",
      "changes": {
        "firstName": "John",
        "email": "john@example.com",
        "status": "ACTIVE"
      },
      "timestamp": "2024-02-01T10:30:00Z"
    },
    {
      "id": 2,
      "entity": "User",
      "entityId": 5,
      "action": "UPDATE",
      "actionBy": "john@example.com",
      "changes": {
        "phoneNumber": "081234567890"
      },
      "timestamp": "2024-02-01T14:20:00Z"
    }
  ]
}
```

### Error Response - Missing Parameters (400 Bad Request)

```json
{
  "success": false,
  "message": "entity and id parameters are required"
}
```

### Error Response - Access Denied (403 Forbidden)

```json
{
  "success": false,
  "message": "Access Denied"
}
```

### Error Response - No Logs Found (200 OK)

```json
{
  "success": true,
  "message": "Audit log fetched",
  "result": []
}
```

---

## Response Format

Audit endpoint mengembalikan `WebResponse` dengan struktur:

```json
{
  "success": boolean,
  "message": string,
  "result": array
}
```

## Audit Actions

| Action | Deskripsi |
|--------|-----------|
| CREATE | Entitas baru dibuat |
| UPDATE | Entitas diperbarui |
| DELETE | Entitas dihapus |
| APPROVE | Entitas disetujui |
| REJECT | Entitas ditolak |
| SUBMIT | Entitas disubmit untuk review |
| REVIEW | Entitas dalam proses review |

## Authentication

- **Get Audit Logs:** Requires authentication
- Access control: User hanya dapat melihat log untuk entitas mereka sendiri (kecuali Admin)
- Admin dapat melihat log untuk semua entitas

## Notes

- Audit logs digunakan untuk tracking perubahan data
- Helpful untuk compliance dan troubleshooting
- Berisi informasi waktu, user yang melakukan action, dan detail perubahan
- Sorted dari paling baru ke paling lama
