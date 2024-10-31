package com.kickstarter.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import com.kickstarter.databinding.WebViewLayoutBinding
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.IntentKey.REFRESH_PROJECT_PAGE
import com.kickstarter.ui.extensions.finishWithAnimation
import com.kickstarter.ui.views.KSWebView

class WebViewActivity : ComponentActivity() {
    private lateinit var binding: WebViewLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WebViewLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbarTitle = intent.getStringExtra(IntentKey.TOOLBAR_TITLE)
        toolbarTitle?.let { binding.webViewToolbar.webViewToolbar.setTitle(it) }

        binding.webView.setDelegate(object : KSWebView.Delegate {
            override fun externalLinkActivated(url: String) {
            }

            override fun pageIntercepted(url: String) {
                if (url.contains("authenticate")) {
                    finish()
                }

                val regex = Regex("""checkouts/\d+/thanks""")

                if (regex.containsMatchIn(url)) {
                    println("Thanks page Pattern found!")
                    finishWithAnimation(withResult = url.toString(), intentKey = REFRESH_PROJECT_PAGE)
                }
            }

            override fun onReceivedError(url: String) {
            }
        })

        val url = intent.getStringExtra(IntentKey.URL)
        url?.let { binding.webView.loadUrl(it) }

        onBackPressedDispatcher.addCallback {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                finishWithAnimation()
            }
        }
    }
}
