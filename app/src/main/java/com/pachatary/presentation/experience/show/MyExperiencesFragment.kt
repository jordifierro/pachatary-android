package com.pachatary.presentation.experience.show

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
import android.widget.ProgressBar
import android.widget.TextView
import com.pachatary.R
import com.pachatary.data.experience.Experience
import com.pachatary.presentation.common.PachataryApplication
import com.pachatary.presentation.experience.edition.CreateExperienceActivity
import com.pachatary.presentation.register.RegisterActivity
import com.pachatary.presentation.scene.show.ExperienceMapActivity
import com.squareup.picasso.Picasso
import javax.inject.Inject

class MyExperiencesFragment : Fragment(), MyExperiencesView {

    companion object {
        fun newInstance() = MyExperiencesFragment()
    }

    @Inject
    lateinit var presenter: MyExperiencesPresenter

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var retryIcon: ImageView
    private lateinit var createExperienceButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PachataryApplication.injector.inject(this)
        presenter.view = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my_experiences, container, false)

        progressBar = view.findViewById(R.id.experiences_progressbar)
        retryIcon = view.findViewById(R.id.experiences_retry)
        retryIcon.setOnClickListener { presenter.onRetryClick() }
        recyclerView = view.findViewById(R.id.experiences_recyclerview)
        recyclerView.layoutManager = GridLayoutManager(activity, 2)
        createExperienceButton = view.findViewById(R.id.create_new_experience_button)
        createExperienceButton.setOnClickListener { presenter.onCreateExperienceClick() }

        presenter.create()

        return view
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
    }

    override fun showLoader() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideLoader() {
        progressBar.visibility = View.GONE
    }

    override fun showRetry() {
        retryIcon.visibility = View.VISIBLE
    }

    override fun hideRetry() {
        retryIcon.visibility = View.GONE
    }

    override fun showExperienceList(experienceList: List<Experience>) {
        recyclerView.adapter = ExperiencesListAdapter(layoutInflater, experienceList,
                { id -> presenter.onExperienceClick(id) })
    }

    override fun navigateToExperience(experienceId: String) {
        startActivity(ExperienceMapActivity.newIntent(activity!!.applicationContext, experienceId))
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

    class ExperiencesListAdapter(private val inflater: LayoutInflater,
                                 private val experienceList: List<Experience>,
                                 val onClick: (String) -> Unit)
                                                    : RecyclerView.Adapter<ExperienceViewHolder>() {
        override fun onBindViewHolder(holder: ExperienceViewHolder, position: Int) {
            holder.bind(experienceList[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperienceViewHolder {
            return ExperienceViewHolder(inflater.inflate(R.layout.item_experiences_list,
                    parent, false), onClick)
        }

        override fun getItemCount(): Int = experienceList.size
    }

    class ExperienceViewHolder(view: View, val onClick: (String) -> Unit)
        : RecyclerView.ViewHolder(view), View.OnClickListener {

        private val titleView: TextView = view.findViewById(R.id.experience_title)
        private val pictureView: ImageView = view.findViewById(R.id.experience_picture)
        lateinit var experienceId: String

        init {
            view.setOnClickListener(this)
        }

        fun bind(experience: Experience) {
            this.experienceId = experience.id
            titleView.text = experience.title
            Picasso.with(pictureView.context)
                    .load(experience.picture?.smallUrl)
                    .into(pictureView)
        }

        override fun onClick(view: View?) = this.onClick(this.experienceId)
    }
}
