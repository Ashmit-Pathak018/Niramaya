package com.example.niramaya.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.niramaya.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    // We add a 'db' reference to check the profile
    val db = FirebaseFirestore.getInstance()

    var isLoading by remember { mutableStateOf(false) }

    // 1. Setup Google Sign In
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    // 2. The Result Handler
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                // Login Success -> Now Check Profile
                firebaseAuthWithGoogle(account.idToken!!, auth, db, context, navController) {
                    isLoading = false
                }
            } else {
                isLoading = false
                Toast.makeText(context, "Google Sign In Failed", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            isLoading = false
            Toast.makeText(context, "Sign In Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // --- UI ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF8F5)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(150.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to Niramaya",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F3D6E)
        )

        Spacer(modifier = Modifier.height(64.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFF0F3D6E))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Checking Profile...", color = Color.Gray)
        } else {
            Button(
                onClick = {
                    isLoading = true
                    launcher.launch(googleSignInClient.signInIntent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(50),
                elevation = ButtonDefaults.buttonElevation(8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(55.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Continue with Google",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// 3. THE "TRAFFIC COP" LOGIC
private fun firebaseAuthWithGoogle(
    idToken: String,
    auth: FirebaseAuth,
    db: FirebaseFirestore, // Passed DB here
    context: Context,
    navController: NavController,
    onComplete: () -> Unit
) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                // CHECK FIRESTORE: Does this user exist?
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // Old User -> Go Home
                            Toast.makeText(context, "Welcome Back!", Toast.LENGTH_SHORT).show()
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            // New User -> Go to Profile Creation
                            Toast.makeText(context, "Please complete your profile", Toast.LENGTH_SHORT).show()
                            navController.navigate("profile") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                        onComplete()
                    }
                    .addOnFailureListener {
                        // If internet fails, default to Home to avoid getting stuck
                        navController.navigate("home")
                        onComplete()
                    }
            } else {
                Toast.makeText(context, "Auth Failed", Toast.LENGTH_SHORT).show()
                onComplete()
            }
        }
}