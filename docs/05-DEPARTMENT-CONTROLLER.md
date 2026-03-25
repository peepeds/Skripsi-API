# Department Controller Documentation

Base URL: `/department`

---

## 1. Get All Departments (Admin Only)

**Endpoint:** `GET /department`

**Access:** Admin role

**Deskripsi:** Mendapatkan daftar lengkap semua departemen/fakultas dengan detail.

### Request

```bash
curl -X GET "http://localhost:8080/department" \
  -H "Authorization: Bearer <admin_access_token>"
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get All Department",
  "result": [
    {
      "deptId": 1,
      "deptName": "SoCS",
      "createdAt": "2024-01-15T08:00:00+07:00",
      "createdBy": "Admin",
      "updatedAt": null,
      "updatedBy": null,
      "active": true
    },
    {
      "deptId": 2,
      "deptName": "SoIS",
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
| deptId | Integer | ID departemen |
| deptName | String | Nama departemen/fakultas |
| createdAt | DateTime | Waktu departemen dibuat |
| createdBy | String | Nama user yang membuat departemen |
| updatedAt | DateTime | Waktu departemen diupdate terakhir |
| updatedBy | String | Nama user yang mengupdate departemen |
| active | Boolean | Status aktif/tidak aktif departemen |

### Error Response

- **403 Forbidden**: User bukan admin
- **401 Unauthorized**: Tidak terautentikasi

---

## 2. Create Department (Admin Only)

**Endpoint:** `POST /department`

**Access:** Admin role

**Deskripsi:** Membuat departemen/fakultas baru di sistem.

### Request Body

```json
{
  "deptName": "SoBE"
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| deptName | String | Ya | Nama departemen (minimal 3 karakter) |

### Example Request

```bash
curl -X POST "http://localhost:8080/department" \
  -H "Authorization: Bearer <admin_access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "deptName": "SoBE"
  }'
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Create Department",
  "result": {
    "deptId": 9,
    "deptName": "SoBE",
    "createdAt": "2024-02-27T10:30:00+07:00",
    "createdBy": "Admin",
    "updatedAt": null,
    "updatedBy": null,
    "active": true
  }
}
```

### Error Response

- **400 Bad Request**: Nama departemen terlalu pendek atau kosong
- **403 Forbidden**: User bukan admin
- **401 Unauthorized**: Tidak terautentikasi

---

## 3. Update Department (Admin Only)

**Endpoint:** `PATCH /department/{deptId}`

**Access:** Admin role

**Deskripsi:** Mengupdate data departemen atau mengubah status aktif/tidak aktif.

### Request Path Parameter

| Parameter | Type | Required | Deskripsi |
|-----------|------|----------|-----------|
| deptId | Integer | Ya | ID departemen yang ingin diupdate |

### Request Body

```json
{
  "deptName": "SoBE - Updated"
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| deptName | String | Tidak | Nama departemen baru |

### Example Request

```bash
curl -X PATCH "http://localhost:8080/department/9" \
  -H "Authorization: Bearer <admin_access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "deptName": "SoBE - Updated"
  }'
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Update Department",
  "result": {
    "deptId": 9,
    "deptName": "SoBE - Updated",
    "createdAt": "2024-02-27T10:30:00+07:00",
    "createdBy": "Admin",
    "updatedAt": "2024-02-27T10:45:00+07:00",
    "updatedBy": "Admin",
    "active": true
  }
}
```

### Error Response

- **400 Bad Request**: Departemen tidak ditemukan atau data tidak valid
- **403 Forbidden**: User bukan admin
- **401 Unauthorized**: Tidak terautentikasi

---

## Important Notes

- **Admin Only**: Semua endpoint di department controller hanya bisa diakses oleh admin
- **Minimal Character**: Department name harus minimal 3 karakter
- **Soft Delete**: Status `active` digunakan untuk soft delete (tidak benar-benar menghapus data)
- **No Get Options**: Berbeda dengan Region, Department tidak punya endpoint `/options` terpisah

