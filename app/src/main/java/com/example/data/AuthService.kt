package com.example.data

import com.example.domain.model.AuthProvider
import com.example.domain.model.UserProfile
import java.util.Locale

class AuthService {
    fun signInWithEmail(email: String, displayName: String? = null): UserProfile {
        val cleanEmail = email.trim().ifEmpty { "utilisateur@safe-network.app" }
        val derived = displayName?.trim()?.takeIf { it.isNotEmpty() }
            ?: cleanEmail.substringBefore("@").ifEmpty { "Utilisateur" }
        val formatted = derived.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        return UserProfile(
            id = "usr-${System.currentTimeMillis()}",
            displayName = formatted,
            email = cleanEmail,
            provider = AuthProvider.EMAIL,
            signedIn = true,
        )
    }

    fun signInWithGoogle(): UserProfile = UserProfile(
        id = "usr-google-demo",
        displayName = "Camille Laurent",
        email = "camille.laurent@gmail.com",
        provider = AuthProvider.GOOGLE,
        signedIn = true,
    )

    fun appleSignInUnavailableMessage(): String =
        "Apple Sign-In est préparé dans l'architecture et n'est pas actif sur Android."

    fun signOut(): UserProfile = UserProfile(
        id = "usr-guest",
        displayName = "Mode local",
        email = "",
        provider = AuthProvider.DEMO,
        signedIn = false,
    )
}
