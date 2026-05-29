package pro.michaelcollard.id34.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;

public class GoogleAuthHelper {
    public static final int RC_SIGN_IN = 1007;
    public static final int RC_AUTH_RECOVER = 1008;
    public static final String DRIVE_SCOPE = "https://www.googleapis.com/auth/drive";
    private final GoogleSignInClient signInClient;

    public GoogleAuthHelper(Context context) {
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DRIVE_SCOPE))
                .build();
        this.signInClient = GoogleSignIn.getClient(context, options);
    }

    public void beginSignIn(Activity activity) {
        Intent intent = signInClient.getSignInIntent();
        activity.startActivityForResult(intent, RC_SIGN_IN);
    }

    public GoogleSignInAccount getLastSignedIn(Context context) {
        return GoogleSignIn.getLastSignedInAccount(context);
    }

    public String getAccessToken(Context context) throws Exception {
        GoogleSignInAccount account = getLastSignedIn(context);
        if (account == null || account.getAccount() == null) {
            throw new IllegalStateException("Google sign-in required before Drive actions.");
        }
        String token;
        try {
            token = GoogleAuthUtil.getToken(context, account.getAccount(), "oauth2:" + DRIVE_SCOPE);
        } catch (UserRecoverableAuthException e) {
            throw e;
        }
        if (TextUtils.isEmpty(token)) {
            throw new IllegalStateException("Unable to obtain Google OAuth access token.");
        }
        return token;
    }
}
