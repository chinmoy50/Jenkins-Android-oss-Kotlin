package com.kickstarter.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Pair
import android.view.View
import androidx.annotation.RequiresApi
import com.kickstarter.R
import com.kickstarter.databinding.PlaygroundLayoutBinding
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.htmlparser.HTMLParser
import com.kickstarter.libs.htmlparser.TextViewElement
import com.kickstarter.libs.htmlparser.getStyledComponents
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.extensions.showSnackbar
import com.kickstarter.viewmodels.PlaygroundViewModel
import rx.android.schedulers.AndroidSchedulers

@RequiresActivityViewModel(PlaygroundViewModel.ViewModel::class)
class PlaygroundActivity : BaseActivity<PlaygroundViewModel.ViewModel?>() {
    private lateinit var binding: PlaygroundLayoutBinding
    private lateinit var view: View

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PlaygroundLayoutBinding.inflate(layoutInflater)
        view = binding.root
        setContentView(view)

        val context = this

        // - Allow clickable spans
        binding.text.linksClickable = true
        binding.text.isClickable = true
        binding.text.movementMethod = LinkMovementMethod.getInstance()

        val headerSize = resources.getDimensionPixelSize(R.dimen.title_3)
        val body = resources.getDimensionPixelSize(R.dimen.callout)
        val html = "<ul>\n" +
            "   <li>This</li>\n" +
            "   <li><strong>is</strong></li>\n" +
            "   <li><em>a</em></li>\n" +
            "   <li><a href=\\\"http://record.pt\\\" target=\\\"_blank\\\" rel=\\\"noopener\\\">list</a></li>\n" +
            "</ul>"

        val html2 = "<ul><li>The 60-page <em><strong>Mother of Monsters Player’s Guide </strong></em>brings your players insights into the politics and geography of their world, alongside new player options to create heroes unique to the Kagari Islands. This spoiler-free resource has all the information your player’s need and nothing they don't, including new races, classes, feats, spells, and even a custom language! </li></ul>"
        val html3 = "<ul><li><em><strong>Epic</strong></em> <em><strong>Treasures</strong></em><strong> ($6 PDF, $13 Print/PDF bundle):</strong> Get 26 pages of marvelous magic items that become even more powerful in the hands of characters with divine blessings and epic boons! Don the <em>cuirass of miracles</em>, seek the <em>golden fleece</em>, sow the <em>teeth</em> <em>of the hydra, </em>and harness the <em>yoke of the brazen bull!</em></li><li><em><strong>Sea Monsters</strong></em> <strong>($15</strong> <strong>PDF, $20 Print/PDF Bundle):</strong> Over 60 maritime monstrosities and nautical nemeses for your heroes, from low-level minions of the deep like mutant<strong>selachim sahuagin</strong> and to <strong>reef hags</strong>, to vast <strong>living islands </strong>and half-alive <strong>coral golems</strong>, plus savage predators like the <strong>devilfish </strong>and <strong>slaughtermaw lamprey</strong>, and legendary foes like the <strong>scylla, charybdis, </strong>and even the <strong>Midgard Serpent</strong>.</li></ul>"

        val listOfElements = HTMLParser().parse(html)
        val element = listOfElements.first() as TextViewElement
        binding.text.text = element.getStyledComponents(body, headerSize, this)

        val listOfElements2 = HTMLParser().parse(html3)

        val element2 = listOfElements2.first() as TextViewElement

        binding.text2.text = element2.getStyledComponents(body, headerSize, this)

        setStepper()
        setProjectActivityButtonClicks()
    }

    /**
     * Set up the stepper example
     */
    private fun setStepper() {
        binding.stepper.inputs.setMinimum(1)
        binding.stepper.inputs.setMaximum(9)
        binding.stepper.inputs.setInitialValue(5)
        binding.stepper.inputs.setVariance(1)

        binding.stepper.outputs.display()
            .compose(bindToLifecycle())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                showSnackbar(binding.stepper, "The updated value on the display is: $it")
            }
    }

    private fun setProjectActivityButtonClicks() {
        binding.newProjectActivity.setOnClickListener { startProjectActivity(Pair(ProjectFactory.project(), RefTag.searchFeatured())) }
    }

    private fun startProjectActivity(projectAndRefTag: Pair<Project, RefTag>) {
        val intent = Intent(this, ProjectPageActivity::class.java)
            .putExtra(IntentKey.PROJECT, projectAndRefTag.first)
            .putExtra(IntentKey.REF_TAG, projectAndRefTag.second)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left)
    }
}
