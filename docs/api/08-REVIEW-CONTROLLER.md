# Review Controller Documentation

Base URL: `/review`

---

## 1. Search Job Options

**Endpoint:** `GET /review/jobOptions`

**Access:** Public

**Deskripsi:** Mencari pilihan pekerjaan/posisi yang tersedia untuk ulasan.

### Query Parameters

| Parameter | Type | Required | Deskripsi |
|-----------|------|----------|-----------|
| query | String | Tidak | Kata kunci pencarian |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully search job options",
  "result": [
    {
      "id": 1,
      "jobTitle": "Backend Developer",
      "department": "Engineering",
      "level": "Senior"
    },
    {
      "id": 2,
      "jobTitle": "Frontend Developer",
      "department": "Engineering",
      "level": "Junior"
    }
  ]
}
```

---

## 2. Get Recent Reviews

**Endpoint:** `GET /review/recent`

**Access:** Public

**Deskripsi:** Mengambil ulasan terbaru yang telah diposting.

### Request

- Method: GET
- No parameters

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully retrieved recent reviews",
  "result": [
    {
      "id": 1,
      "company": "TechCorp Indonesia",
      "rating": 4.5,
      "jobTitle": "Backend Developer",
      "author": "John Doe",
      "createdAt": "2024-01-28T10:30:00Z"
    }
  ]
}
```

---

## 3. Submit Review

**Endpoint:** `POST /review/{slug}`

**Access:** Authenticated users (`isAuthenticated()`)

**Deskripsi:** Mengajukan ulasan perusahaan untuk posisi tertentu.

### Path Parameters

| Parameter | Type | Deskripsi |
|-----------|------|-----------|
| slug | String | URL-friendly identifier perusahaan |

### Request Body

```json
{
  "jobTitle": "Backend Developer",
  "rating": 4.5,
  "salaryMin": 10000000,
  "salaryMax": 15000000,
  "workLifeBalance": 4,
  "compensation": 4,
  "careerGrowth": 5,
  "companyManagement": 4,
  "reviewContent": "Perusahaan yang baik dengan benefit menarik"
}
```

### Request Fields

| Field | Type | Required | Deskripsi |
|-------|------|----------|-----------|
| jobTitle | String | Ya | Judul posisi |
| rating | Float | Ya | Rating keseluruhan (1-5) |
| salaryMin | Long | Tidak | Gaji minimum |
| salaryMax | Long | Tidak | Gaji maksimum |
| workLifeBalance | Integer | Tidak | Rating work-life balance (1-5) |
| compensation | Integer | Tidak | Rating kompensasi (1-5) |
| careerGrowth | Integer | Tidak | Rating pertumbuhan karir (1-5) |
| companyManagement | Integer | Tidak | Rating manajemen (1-5) |
| reviewContent | String | Ya | Isi ulasan |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Review submitted successfully",
  "result": {
    "id": 1,
    "status": "APPROVED",
    "company": "TechCorp Indonesia",
    "jobTitle": "Backend Developer",
    "rating": 4.5,
    "createdAt": "2024-02-01T10:30:00Z"
  }
}
```

### Error Response (401 Unauthorized)

```json
{
  "success": false,
  "message": "Unauthorized"
}
```

---

## 4. Get Company Review Summary

**Endpoint:** `GET /review/{slug}/summary`

**Access:** Public

**Deskripsi:** Mengambil ringkasan/statistik ulasan perusahaan.

### Path Parameters

| Parameter | Type | Deskripsi |
|-----------|------|-----------|
| slug | String | URL-friendly identifier perusahaan |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully retrieved company review summary",
  "result": {
    "company": "TechCorp Indonesia",
    "totalReviews": 120,
    "averageRating": 4.5,
    "averageWorkLifeBalance": 4.2,
    "averageCompensation": 4.4,
    "averageCareerGrowth": 4.6,
    "averageManagement": 4.3,
    "salaryRange": {
      "min": 8000000,
      "max": 20000000
    }
  }
}
```

---

## 5. Get Company Reviews

**Endpoint:** `GET /review/{slug}`

**Access:** Public

**Deskripsi:** Mengambil daftar ulasan perusahaan dengan berbagai filter pengurutan.

### Path Parameters

| Parameter | Type | Deskripsi |
|-----------|------|-----------|
| slug | String | URL-friendly identifier perusahaan |

### Query Parameters

| Parameter | Type | Required | Default | Deskripsi |
|-----------|------|----------|---------|-----------|
| order | String | Tidak | popular | Urutan hasil (popular/recent/highest/lowest) |
| cursor | Long | Tidak | - | Cursor untuk pagination |
| limit | Integer | Tidak | 10 | Jumlah data per halaman |

### Success Response (200 OK)

```json
{
  "success": true,
  "message": "Successfully retrieved company reviews",
  "result": [
    {
      "id": 1,
      "author": "John Doe",
      "jobTitle": "Backend Developer",
      "rating": 4.5,
      "content": "Perusahaan yang baik dengan benefit menarik",
      "salary": {
        "min": 12000000,
        "max": 15000000
      },
      "scores": {
        "workLifeBalance": 4,
        "compensation": 4,
        "careerGrowth": 5,
        "management": 4
      },
      "helpful": 45,
      "createdAt": "2024-01-28T10:30:00Z"
    }
  ],
  "meta": {
    "nextCursor": 2,
    "hasMore": true,
    "limit": 10
  }
}
```

### Error Response (404 Not Found)

```json
{
  "success": false,
  "message": "Company not found"
}
```

---

## Response Format

Semua endpoint mengembalikan `WebResponse` dengan struktur:

```json
{
  "success": boolean,
  "message": string,
  "result": object | array,
  "meta": object (optional)
}
```

## Authentication

- **Search Job Options:** Public
- **Get Recent Reviews:** Public
- **Submit Review:** Requires authentication
- **Get Review Summary:** Public
- **Get Company Reviews:** Public
