package com.pachatary.presentation.experience.show

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.pachatary.data.profile.Profile
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.common.edition.PickAndCropImageActivity
import com.pachatary.presentation.experience.edition.CreateExperienceActivity
import com.pachatary.presentation.experience.show.view.SquareViewHolder
import com.pachatary.presentation.register.RegisterActivity
import com.pachatary.presentation.scene.show.ExperienceScenesActivity
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import javax.inject.Inject


class MyExperiencesFragment : Fragment(), MyExperiencesView {

    companion object {
        fun newInstance() = MyExperiencesFragment()
    }

    @Inject
    lateinit var presenter: MyExperiencesPresenter

    private lateinit var recyclerView: RecyclerView
    private lateinit var createExperienceButton: FloatingActionButton
    private var experiences = listOf<Experience>()

    private val SELECT_IMAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PachataryApplication.injector.inject(this)
        presenter.view = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my_experiences, container, false)

        recyclerView = view.findViewById(R.id.experiences_recyclerview)
        recyclerView.layoutManager = GridLayoutManager(activity, 2)
        createExperienceButton = view.findViewById(R.id.create_new_experience_button)
        createExperienceButton.setOnClickListener { presenter.onCreateExperienceClick() }
        recyclerView.adapter = MyProfileAdapter(layoutInflater,
                { presenter.onExperienceClick(it) }, { presenter.lastExperienceShown() },
                { presenter.onRetryClick() }, { presenter.onProfilePictureClick() })
        (recyclerView.layoutManager as GridLayoutManager).spanSizeLookup =
                object: GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        if (position == 0) return 2
                        if (position == experiences.size + 1) return 2
                        return 1
                    }
                }

        presenter.create()

        return view
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
    }

    override fun showExperiencesLoader() {
        (recyclerView.adapter as MyProfileAdapter).experiencesInProgress = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun hideExperiencesLoader() {
        (recyclerView.adapter as MyProfileAdapter).experiencesInProgress = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showExperiencesRetry() {
        (recyclerView.adapter as MyProfileAdapter).experiencesError = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun hideExperiencesRetry() {
        (recyclerView.adapter as MyProfileAdapter).experiencesError = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showPaginationLoader() {
        (recyclerView.adapter as MyProfileAdapter).paginationInProgress = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun hidePaginationLoader() {
        (recyclerView.adapter as MyProfileAdapter).paginationInProgress = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showExperienceList(experiences: List<Experience>) {
        this.experiences = experiences
        (recyclerView.adapter as MyProfileAdapter).experiences = experiences
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showProfile(profile: Profile) {
        (recyclerView.adapter as MyProfileAdapter).profile = profile
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun hideProfileLoader() {
        (recyclerView.adapter as MyProfileAdapter).profileInProgress = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun hideProfileRetry() {
        (recyclerView.adapter as MyProfileAdapter).profileError = false
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showProfileLoader() {
        (recyclerView.adapter as MyProfileAdapter).profileInProgress = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun showProfileRetry() {
        (recyclerView.adapter as MyProfileAdapter).profileError = true
        recyclerView.adapter.notifyDataSetChanged()
    }

    override fun navigateToExperience(experienceId: String) {
        startActivity(
                ExperienceScenesActivity.newIntent(activity!!.applicationContext, experienceId, true))
    }

    override fun navigateToCreateExperience() {
        startActivity(CreateExperienceActivity.newIntent(context = activity!!.applicationContext))
    }

    override fun showRegisterDialog() {
        val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            AlertDialog.Builder(context!!, android.R.style.Theme_Material_Dialog_Alert)
        else AlertDialog.Builder(context!!)
        builder.setTitle(R.string.dialog_title_mine_register)
                .setMessage(R.string.dialog_question_mine_register)
                .setPositiveButton(android.R.string.yes, { _, _ -> presenter.onProceedToRegister() })
                .setNegativeButton(android.R.string.no, { _, _ -> presenter.onDontProceedToRegister() })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
    }

    override fun navigateToRegister() {
        startActivity(RegisterActivity.newIntent(context = activity!!.applicationContext))
    }

    override fun navigateToPickAndCropImage() {
        startActivityForResult(PickAndCropImageActivity.newIntent(activity!!), SELECT_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK)
            presenter.onImageSelected(PickAndCropImageActivity.getImageUriFrom(data!!))
    }

    class MyProfileAdapter(private val inflater: LayoutInflater,
                           val onExperienceClick: (String) -> Unit,
                           private val onLastItemShown: () -> Unit,
                           private val onRetryClick: () -> Unit,
                           private val onProfilePictureClick: () -> Unit)
        : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var experiencesInProgress = true
        var experiencesError = false
        var paginationInProgress = false
        var experiences: List<Experience> = listOf()

        var profileInProgress = true
        var profileError = false
        var profile: Profile? = null

        val LOADER_TYPE = 0
        val RETRY_TYPE = 1
        val PROFILE_TYPE = 2
        val EXPERIENCE_TYPE = 3

        override fun getItemViewType(position: Int): Int {
            if (position == 0) {
                if (experiencesError || profileError) return RETRY_TYPE
                else if (profileInProgress) return LOADER_TYPE
                else return PROFILE_TYPE
            }
            else if (position == 1) {
                if (experiencesInProgress) return LOADER_TYPE
                else return EXPERIENCE_TYPE
            }
            else if (position == experiences.size + 1) return LOADER_TYPE
            else return EXPERIENCE_TYPE
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (getItemViewType(position)) {
                PROFILE_TYPE -> {
                    val profileViewHolder = holder as ProfileViewHolder
                    profileViewHolder.bind(profile!!)
                }
                EXPERIENCE_TYPE -> {
                    val experienceViewHolder = holder as SquareViewHolder
                    experienceViewHolder.bind(experiences[position-1])
                }
                else -> {}
            }

            if (!experiencesError && !experiencesInProgress) {
                val endHasBeenReached = position == experiences.size
                if (experiences.isNotEmpty() && endHasBeenReached) onLastItemShown.invoke()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            when (viewType) {
                LOADER_TYPE -> return object : RecyclerView.ViewHolder(
                        inflater.inflate(R.layout.item_loader, parent, false)) {}
                RETRY_TYPE -> {
                    val view = inflater.inflate(R.layout.item_retry, parent, false)
                    val viewHolder =  object : RecyclerView.ViewHolder(view), View.OnClickListener {
                        override fun onClick(v: View?) { onRetryClick.invoke() } }
                    view.setOnClickListener(viewHolder)
                    return viewHolder
                }
                PROFILE_TYPE ->
                    return ProfileViewHolder(inflater.inflate(R.layout.item_profile, parent, false),
                                             onProfilePictureClick)
                else ->
                    return SquareViewHolder(
                            inflater.inflate(R.layout.item_square_experiences_list, parent, false),
                            onExperienceClick)
            }
        }

        override fun getItemCount(): Int {
            if (experiencesError || profileError) return 1
            else if (profileInProgress && experiencesInProgress) return 1
            else if (profileInProgress) return experiences.size + 1
            else if (experiencesInProgress) return 2
            else if (paginationInProgress) return experiences.size + 2
            else return experiences.size + 1
        }

        class ProfileViewHolder(view: View, val onProfilePictureClick: () -> Unit)
                                                                   : RecyclerView.ViewHolder(view) {

            private val usernameView: TextView = view.findViewById(R.id.username)
            private val bioView: TextView = view.findViewById(R.id.bio)
            private val pictureView: ImageView = view.findViewById(R.id.picture)

            fun bind(profile: Profile) {
                usernameView.text = profile.username
                bioView.text = profile.bio
                Picasso.with(pictureView.context)
                        .load(profile.picture?.smallUrl)
                        .transform(CropCircleTransformation())
                        .into(pictureView)
                pictureView.setOnClickListener { onProfilePictureClick.invoke() }
            }
        }
    }
}
