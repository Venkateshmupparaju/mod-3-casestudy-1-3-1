package project.rag_basedassistant3_1

import okhttp3.MultipartBody
import project.rag_basedassistant3_1.models.QueryRequest
import project.rag_basedassistant3_1.models.QueryResponse
import project.rag_basedassistant3_1.models.UploadResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("upload")
    suspend fun uploadDocument(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @POST("query")
    suspend fun queryRAG(
        @Body request: QueryRequest
    ): Response<QueryResponse>
}
