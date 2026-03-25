# Company Controller Documentation

Base URL: `/company`

---

## 1. Get All Companies

**Endpoint:** `GET /company`

**Access:** Publik (Tidak perlu login)

**Deskripsi:** Mendapatkan daftar perusahaan dengan pagination.

### Request Parameters

| Parameter | Type | Required | Default | Deskripsi |
|-----------|------|----------|---------|-----------|
| page | Integer | Tidak | 0 | Nomor halaman (dimulai dari 0) |
| limit | Integer | Tidak | 15 | Jumlah data per halaman |

### Example Request

```bash
curl -X GET "http://localhost:8080/company?page=0&limit=15"
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully Get Companies data",
  "meta": {
    "page": 0,
    "size": 15,
    "totalElements": 50,
    "totalPages": 4,
    "hasNext": true,
    "hasPrevious": false
  },
  "result": [
    {
      "companyId": 1,
      "companyName": "PT Maju Jaya Indonesia",
      "companySlug": "pt-maju-jaya-indonesia"
    },
    {
      "companyId": 2,
      "companyName": "PT Digital Solutions",
      "companySlug": "pt-digital-solutions"
    }
  ]
}
```

### Response Fields

| Field | Type | Deskripsi |
|-------|------|-----------|
| page | Integer | Nomor halaman saat ini |
| size | Integer | Jumlah data per halaman |
| totalElements | Long | Total jumlah perusahaan |
| totalPages | Integer | Total jumlah halaman |
| hasNext | Boolean | Ada halaman berikutnya? |
| hasPrevious | Boolean | Ada halaman sebelumnya? |
| companyId | Long | ID perusahaan |
| companyName | String | Nama perusahaan |
| companySlug | String | Slug unik perusahaan (untuk URL/link) |

---

## 2. Submit Company Request

**Endpoint:** `POST /company/requests`

**Access:** Autentikasi diperlukan (User login)

**Deskripsi:** Mengajukan permintaan untuk menambahkan perusahaan baru yang belum ada di sistem.

### Request Body

```json
{
  "companyName": "PT Contoh Internasional",
  "companyAbbreviation": "PT CI",
  "website": "https://contoh.co.id",
  "isPartner": true
}
```

### Request Fields

| Field               | Type    | Required | Deskripsi                                      |
|---------------------|---------|----------|------------------------------------------------|
| companyName         | String  | Ya       | Nama perusahaan (3-65 karakter)                |
| companyAbbreviation | String  | Tidak    | Singkatan nama perusahaan (maksimal 15 karakter). Jika tidak diisi/null, otomatis diambil dari inisial nama perusahaan (misal: "Bank Central Asia" → "BCA") |
| website             | String  | Tidak    | Website perusahaan (maksimal 35 karakter)      |
| isPartner           | Boolean | Tidak    | Apakah perusahaan merupakan partner?           |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully submit company request",
  "result": {
    "companyRequestId": 1,
    "companyName": "PT Contoh Internasional",
    "companyAbbreviation": "PT CI",
    "website": "https://contoh.co.id",
    "isPartner": true,
    "status": "PENDING",
    "createdAt": "2024-02-27T10:30:00+07:00",
    "createdBy": "John Doe",
    "reviewedAt": null,
    "reviewedBy": null,
    "reviewNote": null
  }
}
```

### Error Response

- **400 Bad Request**: Data tidak valid (nama terlalu pendek, dll)
- **401 Unauthorized**: Tidak terautentikasi

---

## 3. Get Company Requests (Admin Only)

**Endpoint:** `GET /company/requests`

**Access:** Admin role

**Deskripsi:** Melihat daftar permintaan penambahan perusahaan (untuk review oleh admin).

### Request Parameters

| Parameter | Type | Required | Default | Deskripsi |
|-----------|------|----------|---------|-----------|
| status | String | Tidak | - | Filter status: `PENDING`, `APPROVED`, `REJECTED` |
| page | Integer | Tidak | 0 | Nomor halaman (dimulai dari 0) |
| limit | Integer | Tidak | 15 | Jumlah data per halaman |

### Example Request

```bash
# Lihat semua request
curl -X GET "http://localhost:8080/company/requests?page=0&limit=15" \
  -H "Authorization: Bearer <access_token>"

# Lihat hanya request yang pending
curl -X GET "http://localhost:8080/company/requests?status=PENDING&page=0&limit=15" \
  -H "Authorization: Bearer <access_token>"
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully get company requests",
  "meta": {
    "page": 0,
    "size": 15,
    "totalElements": 25,
    "totalPages": 2,
    "hasNext": true,
    "hasPrevious": false
  },
  "result": [
    {
      "companyRequestId": 1,
      "companyName": "PT Contoh Internasional",
      "website": "https://contoh.co.id",
      "status": "PENDING",
      "createdAt": "2024-02-27T10:30:00+07:00",
      "createdBy": "John Doe",
      "reviewedAt": null,
      "reviewedBy": null,
      "reviewNote": null
    },
    {
      "companyRequestId": 2,
      "companyName": "PT Tech Innovation",
      "website": "https://tech-inn.com",
      "status": "PENDING",
      "createdAt": "2024-02-26T14:15:00+07:00",
      "createdBy": "Jane Smith",
      "reviewedAt": null,
      "reviewedBy": null,
      "reviewNote": null
    }
  ]
}
```

### Error Response

- **403 Forbidden**: User bukan admin
- **401 Unauthorized**: Tidak terautentikasi

---

## 4. Review Company Request (Admin Only)

**Endpoint:** `PATCH /company/requests/{requestId}/review`

**Access:** Admin role

**Deskripsi:** Approval atau rejection terhadap permintaan penambahan perusahaan. Jika approve, perusahaan akan ditambahkan ke database secara otomatis.

### Request Path Parameter

| Parameter | Type | Required | Deskripsi |
|-----------|------|----------|-----------|
| requestId | Long | Ya | ID dari company request |

### Request Body

```json
{
  "status": "APPROVED",
  "reviewNote": "Data perusahaan lengkap dan valid, disetujui."
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| status | String | Ya | Status hasil review: `APPROVED` atau `REJECTED` |
| reviewNote | String | Tidak | Catatan dari admin (maksimal 255 karakter) |

### Example Request

**Approve:**
```bash
curl -X PATCH "http://localhost:8080/company/requests/1/review" \
  -H "Authorization: Bearer <admin_access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "APPROVED",
    "reviewNote": "Data perusahaan lengkap, disetujui."
  }'
```

**Reject:**
```bash
curl -X PATCH "http://localhost:8080/company/requests/1/review" \
  -H "Authorization: Bearer <admin_access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "REJECTED",
    "reviewNote": "Nama perusahaan tidak valid / website tidak aktif."
  }'
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully review company request",
  "result": {
    "companyRequestId": 1,
    "companyName": "PT Contoh Internasional",
    "website": "https://contoh.co.id",
    "status": "APPROVED",
    "createdAt": "2024-02-27T10:30:00+07:00",
    "createdBy": "John Doe",
    "reviewedAt": "2024-02-27T11:45:00+07:00",
    "reviewedBy": "Admin User",
    "reviewNote": "Data perusahaan lengkap, disetujui."
  }
}
```

### Error Response

- **400 Bad Request**: Data tidak valid atau company request tidak ditemukan
- **403 Forbidden**: User bukan admin
- **401 Unauthorized**: Tidak terautentikasi

---


## Status Enum

Status yang valid untuk company request:

| Status | Deskripsi |
|--------|-----------|
| PENDING | Permintaan baru, menunggu review dari admin |
| APPROVED | Permintaan disetujui, perusahaan sudah ditambahkan |
| REJECTED | Permintaan ditolak, perusahaan tidak ditambahkan |

---

## 5. Get Company Request Detail

**Endpoint:** `GET /company/request/{requestId}`

**Access:** Autentikasi diperlukan (User login)

**Deskripsi:** Mendapatkan detail lengkap dari sebuah company request, termasuk info submitter, reviewer, dan dokumen terkait. Endpoint ini dituju dari inbox via `referenceId`.

### Request Path Parameter

| Parameter | Type | Required | Deskripsi |
|-----------|------|----------|-----------|
| requestId | Long | Ya | ID dari company request |

### Example Request

```bash
curl -X GET "http://localhost:8080/company/request/4" \
  -H "Authorization: Bearer <access_token>"
```

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Company request detail",
  "result": {
    "requestDetails": {
      "id": 4,
      "companyName": "PT Hidup Internasional",
      "companyAbbreviation": "PT HI",
      "website": "https://hidup-intl.co.id",
      "submittedBy": "Edbert",
      "submittedAt": "2026-02-27T17:24:01Z",
      "documents": [
        {
          "fileName": "npwp.pdf",
          "url": "/files/npwp.pdf"
        }
      ]
    },
    "reviewInformation": {
      "status": "APPROVED",
      "reviewNote": "Data valid dan lengkap.",
      "reviewedBy": "Admin",
      "reviewedAt": "2026-02-27T17:28:20Z"
    }
  }
}
```

### Response Fields

| Field | Type | Deskripsi |
|-------|------|-----------|
| requestDetails.id | Long | ID dari company request |
| requestDetails.companyName | String | Nama perusahaan yang diajukan |
| requestDetails.companyAbbreviation | String | Singkatan nama perusahaan |
| requestDetails.website | String | Website perusahaan (bisa null) |
| requestDetails.submittedBy | String | Nama user yang submit |
| requestDetails.submittedAt | DateTime | Waktu submission |
| requestDetails.documents | Array | Daftar dokumen terlampir |
| requestDetails.documents[].fileName | String | Nama file |
| requestDetails.documents[].url | String | URL file |
| reviewInformation.status | String | Status: `PENDING`, `APPROVED`, `REJECTED` |
| reviewInformation.reviewNote | String | Catatan review (null jika belum direview) |
| reviewInformation.reviewedBy | String | Nama admin yang review (null jika belum direview) |
| reviewInformation.reviewedAt | DateTime | Waktu review (null jika belum direview) |

### Error Response

- **400 Bad Request**: Company request tidak ditemukan
- **401 Unauthorized**: Tidak terautentikasi

---

## Important Notes

- **User Submission**: Pengguna yang terautentikasi dapat submit request kapan saja, admin yang akan handle saat review
- **Auto Creation**: Ketika admin approve request, perusahaan otomatis dibuat di tabel `companies`
- **Admin Authority**: Admin memiliki kontrol penuh atas proses review dan filtering
- **Audit Trail**: Semua aksi (submit, approve, reject) tercatat di `GET /audit?entity=COMPANY_REQUEST&id={id}` (lihat [09-AUDIT-CONTROLLER.md](./09-AUDIT-CONTROLLER.md))
- **Inbox**: Notifikasi status request tersedia di `GET /inbox` (lihat [08-INBOX-CONTROLLER.md](./08-INBOX-CONTROLLER.md))
