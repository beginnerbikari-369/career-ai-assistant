package com.careerai.data.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    companion object {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_CONVERSATIONS = "conversations"
        const val COLLECTION_MESSAGES = "messages"
        const val COLLECTION_GOALS = "goals"
        const val COLLECTION_HABITS = "habits"
        const val COLLECTION_HABIT_COMPLETIONS = "habit_completions"
        const val COLLECTION_CALENDAR_EVENTS = "calendar_events"
        const val COLLECTION_JOURNAL_ENTRIES = "journal_entries"
        const val COLLECTION_SKILLS = "skills"
        const val COLLECTION_JOB_APPLICATIONS = "job_applications"
        const val COLLECTION_SYNC_STATUS = "sync_status"
    }
    
    // Generic CRUD operations
    suspend fun <T> addDocument(
        collection: String,
        document: T,
        documentId: String? = null
    ): Result<String> {
        return try {
            val docRef = if (documentId != null) {
                firestore.collection(collection).document(documentId)
            } else {
                firestore.collection(collection).document()
            }
            
            docRef.set(document!!).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun <T> getDocument(
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): Result<T?> {
        return try {
            val snapshot = firestore.collection(collection).document(documentId).get().await()
            val document = snapshot.toObject(clazz)
            Result.success(document)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun <T> updateDocument(
        collection: String,
        documentId: String,
        updates: Map<String, Any>
    ): Result<Unit> {
        return try {
            firestore.collection(collection).document(documentId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteDocument(collection: String, documentId: String): Result<Unit> {
        return try {
            firestore.collection(collection).document(documentId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun <T> getDocumentFlow(
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): Flow<T?> = callbackFlow {
        val listener = firestore.collection(collection).document(documentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val document = snapshot?.toObject(clazz)
                trySend(document)
            }
        
        awaitClose { listener.remove() }
    }
    
    fun <T> getCollectionFlow(
        collection: String,
        clazz: Class<T>,
        whereClause: ((Query) -> Query)? = null
    ): Flow<List<T>> = callbackFlow {
        var query: Query = firestore.collection(collection)
        
        whereClause?.let { query = it(query) }
        
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val documents = snapshot?.documents?.mapNotNull { it.toObject(clazz) } ?: emptyList()
            trySend(documents)
        }
        
        awaitClose { listener.remove() }
    }
    
    suspend fun <T> getCollection(
        collection: String,
        clazz: Class<T>,
        whereClause: ((Query) -> Query)? = null
    ): Result<List<T>> {
        return try {
            var query: Query = firestore.collection(collection)
            
            whereClause?.let { query = it(query) }
            
            val snapshot = query.get().await()
            val documents = snapshot.documents.mapNotNull { it.toObject(clazz) }
            Result.success(documents)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // User-specific operations
    suspend fun <T> addUserDocument(
        userId: String,
        collection: String,
        document: T,
        documentId: String? = null
    ): Result<String> {
        return addDocument("$COLLECTION_USERS/$userId/$collection", document, documentId)
    }
    
    suspend fun <T> getUserDocument(
        userId: String,
        collection: String,
        documentId: String,
        clazz: Class<T>
    ): Result<T?> {
        return getDocument("$COLLECTION_USERS/$userId/$collection", documentId, clazz)
    }
    
    fun <T> getUserCollectionFlow(
        userId: String,
        collection: String,
        clazz: Class<T>,
        whereClause: ((Query) -> Query)? = null
    ): Flow<List<T>> {
        return getCollectionFlow("$COLLECTION_USERS/$userId/$collection", clazz, whereClause)
    }
    
    suspend fun <T> getUserCollection(
        userId: String,
        collection: String,
        clazz: Class<T>,
        whereClause: ((Query) -> Query)? = null
    ): Result<List<T>> {
        return getCollection("$COLLECTION_USERS/$userId/$collection", clazz, whereClause)
    }
    
    // Batch operations
    suspend fun batchWrite(operations: List<BatchOperation>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            
            operations.forEach { operation ->
                when (operation) {
                    is BatchOperation.Set -> {
                        val docRef = firestore.collection(operation.collection)
                            .document(operation.documentId)
                        batch.set(docRef, operation.data)
                    }
                    is BatchOperation.Update -> {
                        val docRef = firestore.collection(operation.collection)
                            .document(operation.documentId)
                        batch.update(docRef, operation.updates)
                    }
                    is BatchOperation.Delete -> {
                        val docRef = firestore.collection(operation.collection)
                            .document(operation.documentId)
                        batch.delete(docRef)
                    }
                }
            }
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Sync status operations
    suspend fun updateSyncStatus(
        userId: String,
        collection: String,
        lastSyncTime: Long
    ): Result<Unit> {
        val syncData = mapOf(
            "collection" to collection,
            "lastSyncTime" to lastSyncTime,
            "userId" to userId
        )
        return addDocument(COLLECTION_SYNC_STATUS, syncData, "${userId}_$collection").map { Unit }
    }
    
    suspend fun getSyncStatus(userId: String, collection: String): Result<Long?> {
        return try {
            val snapshot = firestore.collection(COLLECTION_SYNC_STATUS)
                .document("${userId}_$collection")
                .get()
                .await()
            
            val lastSyncTime = snapshot.getLong("lastSyncTime")
            Result.success(lastSyncTime)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

sealed class BatchOperation {
    data class Set(
        val collection: String,
        val documentId: String,
        val data: Any
    ) : BatchOperation()
    
    data class Update(
        val collection: String,
        val documentId: String,
        val updates: Map<String, Any>
    ) : BatchOperation()
    
    data class Delete(
        val collection: String,
        val documentId: String
    ) : BatchOperation()
}