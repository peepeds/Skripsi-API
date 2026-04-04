# Dokumentasi API — Audit Log

Base path: `/audit`

---

## GET /audit

**Autentikasi**: Harus login

Mengambil daftar audit log untuk entitas tertentu berdasarkan nama entitas dan ID-nya. Hanya mengembalikan log yang relevan dengan pengguna yang sedang login.

### Query Parameters

| Parameter | Tipe   | Wajib | Keterangan                                           |
|-----------|--------|-------|------------------------------------------------------|
| entity    | string | Ya    | Nama entitas yang ingin dicek, contoh: `Company`, `Certificate` |
| id        | long   | Ya    | ID dari entitas yang bersangkutan                    |

### Contoh Request

```
GET /audit?entity=Certificate&id=5
```

### Contoh Response

```json
{
  "success": true,
  "message": "Audit log fetched",
  "result": [
    {
      "id": 201,
      "action": "UPDATE",
      "entityName": "Certificate",
      "entityId": 5,
      "changedBy": 1,
      "changedAt": "2026-04-02T11:00:00",
      "description": "Status changed from PENDING to APPROVED"
    },
    {
      "id": 198,
      "action": "CREATE",
      "entityName": "Certificate",
      "entityId": 5,
      "changedBy": 1,
      "changedAt": "2026-04-02T10:00:00",
      "description": "Certificate request submitted"
    }
  ]
}
```
