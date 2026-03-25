# Region Controller Documentation

Base URL: `/region`

---

## 1. Get All Regions

**Endpoint:** `GET /region`

**Access:** Publik (Tidak perlu login)

**Deskripsi:** Mendapatkan daftar lengkap semua region/kampus dengan detail.

### Request

```bash
curl -X GET "http://localhost:8080/region"
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get All Regions Data",
  "result": [
    {
      "regionId": 1,
      "regionName": "Kemanggisan",
      "createdAt": "2024-01-15T08:00:00+07:00",
      "createdBy": "Admin",
      "updatedAt": null,
      "updatedBy": null,
      "active": true
    },
    {
      "regionId": 2,
      "regionName": "Alam Sutera",
      "createdAt": "2024-01-15T08:05:00+07:00",
      "createdBy": "Admin",
      "updatedAt": null,
      "updatedBy": null,
      "active": true
    }
  ]
}
```

### Response Fields

| Field | Type | Deskripsi |
|-------|------|-----------|
| regionId | Integer | ID region |
| regionName | String | Nama region/kampus |
| createdAt | DateTime | Waktu region dibuat |
| createdBy | String | Nama user yang membuat region |
| updatedAt | DateTime | Waktu region diupdate terakhir |
| updatedBy | String | Nama user yang mengupdate region |
| active | Boolean | Status aktif/tidak aktif region |

---

## 2. Get Region Options (Simple List)

**Endpoint:** `GET /region/options`

**Access:** Publik (Tidak perlu login)

**Deskripsi:** Mendapatkan daftar region dalam format sederhana hanya dengan ID dan nama (untuk dropdown/select).

### Request

```bash
curl -X GET "http://localhost:8080/region/options"
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get Regions",
  "result": [
    {
      "regionId": 1,
      "regionName": "Kemanggisan"
    },
    {
      "regionId": 2,
      "regionName": "Alam Sutera"
    },
    {
      "regionId": 3,
      "regionName": "Bekasi"
    }
  ]
}
```

---

## 3. Create Region (Admin Only)

**Endpoint:** `POST /region`

**Access:** Admin role

**Deskripsi:** Membuat region/kampus baru di sistem.

### Request Body

```json
{
  "regionName": "Jakarta Pusat"
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| regionName | String | Ya | Nama region (minimal 5 karakter) |

### Example Request

```bash
curl -X POST "http://localhost:8080/region" \
  -H "Authorization: Bearer <admin_access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "regionName": "Jakarta Pusat"
  }'
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Created new Region",
  "result": {
    "regionId": 9,
    "regionName": "Jakarta Pusat",
    "createdAt": "2024-02-27T10:30:00+07:00",
    "createdBy": "Admin",
    "updatedAt": null,
    "updatedBy": null,
    "active": true
  }
}
```

### Error Response

- **400 Bad Request**: Nama region terlalu pendek atau kosong
- **403 Forbidden**: User bukan admin
- **401 Unauthorized**: Tidak terautentikasi

---

## 4. Update Region (Admin Only)

**Endpoint:** `PATCH /region/{regionId}`

**Access:** Admin role

**Deskripsi:** Mengupdate data region atau mengubah status aktif/tidak aktif.

### Request Path Parameter

| Parameter | Type | Required | Deskripsi |
|-----------|------|----------|-----------|
| regionId | Integer | Ya | ID region yang ingin diupdate |

### Request Body

```json
{
  "regionName": "Jakarta Pusat - Updated",
  "active": true
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| regionName | String | Tidak | Nama region baru |
| active | Boolean | Tidak | Status aktif/tidak aktif region |

### Example Request

```bash
# Update nama dan status
curl -X PATCH "http://localhost:8080/region/9" \
  -H "Authorization: Bearer <admin_access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "regionName": "Jakarta Pusat - Updated",
    "active": false
  }'

# Update hanya nama
curl -X PATCH "http://localhost:8080/region/9" \
  -H "Authorization: Bearer <admin_access_token>" \
  -H "Content-Type: application/json" \
  -d '{"regionName": "Jakarta Barat"}'

# Update hanya status
curl -X PATCH "http://localhost:8080/region/9" \
  -H "Authorization: Bearer <admin_access_token>" \
  -H "Content-Type: application/json" \
  -d '{"active": false}'
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Updated Region",
  "result": {
    "regionId": 9,
    "regionName": "Jakarta Pusat - Updated",
    "createdAt": "2024-02-27T10:30:00+07:00",
    "createdBy": "Admin",
    "updatedAt": "2024-02-27T10:45:00+07:00",
    "updatedBy": "Admin",
    "active": false
  }
}
```

### Error Response

- **400 Bad Request**: Region tidak ditemukan atau data tidak valid
- **403 Forbidden**: User bukan admin
- **401 Unauthorized**: Tidak terautentikasi

---

## Important Notes

- **Public Endpoints**: GET /region dan GET /region/options bersifat publik untuk user experience yang lebih baik
- **Admin Only**: CREATE dan UPDATE region hanya bisa dilakukan oleh admin
- **Soft Delete**: Update status `active` ke `false` adalah cara untuk "menghapus" region tanpa menghilangkan data historis
- **Minimal Character**: Region name harus minimal 5 karakter

