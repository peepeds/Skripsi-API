# Major Controller Documentation

Base URL: `/major`

---

## 1. Get All Majors (Admin Only)

**Endpoint:** `GET /major`

**Access:** Admin role

**Deskripsi:** Mendapatkan daftar lengkap semua jurusan/prodi dengan detail termasuk departemen dan region.

### Request

```bash
curl -X GET "http://localhost:8080/major" \
  -H "Authorization: Bearer <admin_access_token>"
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "successfully Get All Major",
  "result": [
    {
      "majorId": 1,
      "majorName": "Computer Science",
      "deptName": "SoCS",
      "regionName": "Kemanggisan",
      "createdAt": "2024-01-15T08:00:00+07:00",
      "createdBy": "Admin",
      "updatedAt": null,
      "updatedBy": null,
      "active": true
    },
    {
      "majorId": 2,
      "majorName": "Cyber Security",
      "deptName": "SoCS",
      "regionName": "Kemanggisan",
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
| majorId | Integer | ID jurusan |
| majorName | String | Nama jurusan/prodi |
| deptName | String | Nama departemen yang memiliki jurusan ini |
| regionName | String | Nama region/kampus yang memiliki jurusan ini |
| createdAt | DateTime | Waktu jurusan dibuat |
| createdBy | String | Nama user yang membuat jurusan |
| updatedAt | DateTime | Waktu jurusan diupdate terakhir |
| updatedBy | String | Nama user yang mengupdate jurusan |
| active | Boolean | Status aktif/tidak aktif jurusan |

### Error Response

- **403 Forbidden**: User bukan admin
- **401 Unauthorized**: Tidak terautentikasi

---

## 2. Get Major Options (Simple List)

**Endpoint:** `GET /major/options`

**Access:** Publik (Tidak perlu login)

**Deskripsi:** Mendapatkan daftar jurusan dalam format sederhana hanya dengan ID dan nama (untuk dropdown/select di form registrasi).

### Request

```bash
curl -X GET "http://localhost:8080/major/options"
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get Major",
  "result": [
    {
      "majorId": 1,
      "majorName": "Computer Science"
    },
    {
      "majorId": 2,
      "majorName": "Cyber Security"
    },
    {
      "majorId": 3,
      "majorName": "Data Science"
    }
  ]
}
```

---

## 3. Create Major (Admin Only)

**Endpoint:** `POST /major`

**Access:** Admin role

**Deskripsi:** Membuat jurusan/prodi baru di sistem.

### Request Body

```json
{
  "majorName": "Cloud Computing",
  "deptId": 1,
  "regionId": 1
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| majorName | String | Ya | Nama jurusan (minimal 5 karakter) |
| deptId | Integer | Ya | ID departemen yang memiliki jurusan ini |
| regionId | Integer | Ya | ID region/kampus tempat jurusan ini |

### Example Request

```bash
curl -X POST "http://localhost:8080/major" \
  -H "Authorization: Bearer <admin_access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "majorName": "Cloud Computing",
    "deptId": 1,
    "regionId": 1
  }'
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Created New Major",
  "result": {
    "majorId": 50,
    "majorName": "Cloud Computing",
    "deptName": "SoCS",
    "regionName": "Kemanggisan",
    "createdAt": "2024-02-27T10:30:00+07:00",
    "createdBy": "Admin",
    "updatedAt": null,
    "updatedBy": null,
    "active": true
  }
}
```

### Error Response

- **400 Bad Request**: Nama jurusan terlalu pendek, deptId atau regionId tidak valid
- **403 Forbidden**: User bukan admin
- **401 Unauthorized**: Tidak terautentikasi

---

## 4. Update Major (Admin Only)

**Endpoint:** `PATCH /major/{majorId}`

**Access:** Admin role

**Deskripsi:** Mengupdate data jurusan, mengubah departemen/region, atau mengubah status aktif/tidak aktif.

### Request Path Parameter

| Parameter | Type | Required | Deskripsi |
|-----------|------|----------|-----------|
| majorId | Integer | Ya | ID jurusan yang ingin diupdate |

### Request Body

```json
{
  "majorName": "Cloud Computing - Advanced",
  "deptId": 1,
  "regionId": 2,
  "active": true
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| majorName | String | Tidak | Nama jurusan baru (minimal 5 karakter) |
| deptId | Integer | Tidak | ID departemen yang baru |
| regionId | Integer | Tidak | ID region/kampus yang baru |
| active | Boolean | Tidak | Status aktif/tidak aktif jurusan |

### Example Request

```bash
# Update nama dan status
curl -X PATCH "http://localhost:8080/major/50" \
  -H "Authorization: Bearer <admin_access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "majorName": "Cloud Computing - Advanced",
    "active": true
  }'

# Update hanya nama
curl -X PATCH "http://localhost:8080/major/50" \
  -H "Authorization: Bearer <admin_access_token>" \
  -H "Content-Type: application/json" \
  -d '{"majorName": "Distributed Systems"}'

# Disable jurusan
curl -X PATCH "http://localhost:8080/major/50" \
  -H "Authorization: Bearer <admin_access_token>" \
  -H "Content-Type: application/json" \
  -d '{"active": false}'

# Pindahkan ke departemen dan region lain
curl -X PATCH "http://localhost:8080/major/50" \
  -H "Authorization: Bearer <admin_access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "deptId": 2,
    "regionId": 3
  }'
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Updated Major",
  "result": {
    "majorId": 50,
    "majorName": "Cloud Computing - Advanced",
    "deptName": "SoCS",
    "regionName": "Alam Sutera",
    "createdAt": "2024-02-27T10:30:00+07:00",
    "createdBy": "Admin",
    "updatedAt": "2024-02-27T10:45:00+07:00",
    "updatedBy": "Admin",
    "active": true
  }
}
```

### Error Response

- **400 Bad Request**: Major tidak ditemukan atau data tidak valid
- **403 Forbidden**: User bukan admin
- **401 Unauthorized**: Tidak terautentikasi

---

## Important Notes

- **Public Options Endpoint**: GET /major/options bersifat publik untuk form registrasi, tidak perlu login
- **Admin Management**: GET /major (detail lengkap) hanya untuk admin
- **Hierarchical**: Jurusan tergantung pada departemen dan region
- **Minimal Character**: Major name harus minimal 5 karakter
- **Soft Delete**: Status `active` ke `false` digunakan untuk soft delete tanpa menghilangkan data historis

