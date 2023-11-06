package com.Family.securefamily

import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.Family.securefamily.R
import com.Family.securefamily.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList


class HomeFragment : Fragment() {
    lateinit var inviteAdapter: InviteAdapter
    lateinit var mContext: Context
    private val listContacts: ArrayList<ContactModel> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getView()?.findViewById<Button>(R.id.ite_button)?.setOnClickListener {
            Toast.makeText(context, "Invite Sent", Toast.LENGTH_LONG).show()
        }

        Log.d("FetchContact89", "onViewCreated: ")

        val listMembers = listOf<MemberModel>(
            MemberModel(
                "Lakshami",
                "Qutub Vihar 1, Goyal Village, Delhi , 110071",
                "50%",
                "1300m"
            ),
            MemberModel(
                "Avinash Kumar",
                "Sector 16D Dwarka, Kakrola, Delhi, 110075",
                "34%",
                "950m"
            ),
        )


        val adapter = MemberAdapter(listMembers)

        binding.recyclerMember.layoutManager = LinearLayoutManager(mContext)
        binding.recyclerMember.adapter = adapter



        Log.d("FetchContact89", "fetchContacts: start karne wale hai")

        Log.d("FetchContact89", "fetchContacts: start hogya hai ${listContacts.size}")
        inviteAdapter = InviteAdapter(listContacts)
        fetchDatabaseContacts()
        Log.d("FetchContact89", "fetchContacts: end hogya hai")

        CoroutineScope(Dispatchers.IO).launch {
            Log.d("FetchContact89", "fetchContacts: coroutine start")

            insertDatabaseContacts(fetchContacts())

            Log.d("FetchContact89", "fetchContacts: coroutine end ${listContacts.size}")
        }



        binding.recyclerInvite.layoutManager =
            LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerInvite.adapter = inviteAdapter



        binding.threeDots.setOnClickListener {

            SharedPref.putBoolean(PrefConstants.IS_USER_LOGGED_IN, false)

            FirebaseAuth.getInstance().signOut()

        }

    }

    private fun fetchDatabaseContacts() {
        val database = MyFamilyDatabase.getDatabase(mContext)

        database.contactDao().getAllContacts().observe(viewLifecycleOwner) {

            Log.d("FetchContact89", "fetchDatabaseContacts: ")

            listContacts.clear()
            listContacts.addAll(it)

            inviteAdapter.notifyDataSetChanged()

        }
    }

    private suspend fun insertDatabaseContacts(listContacts: ArrayList<ContactModel>) {

        val database = MyFamilyDatabase.getDatabase(mContext)

        database.contactDao().insertAll(listContacts)

    }


    private fun fetchContacts(): ArrayList<ContactModel> {

        Log.d("FetchContact89", "fetchContacts: start")
        val cr = mContext.contentResolver
        val cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

        val listContacts: ArrayList<ContactModel> = ArrayList()


        if (cursor != null && cursor.count > 0) {

            while (cursor != null && cursor.moveToNext()) {
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val hasPhoneNumber =
                    cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                if (hasPhoneNumber > 0) {

                    val pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        ""
                    )

                    if (pCur != null && pCur.count > 0) {

                        while (pCur != null && pCur.moveToNext()) {

                            val phoneNum =
                                pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                            listContacts.add(ContactModel(name, phoneNum))

                        }
                        pCur.close()

                    }

                }
            }

            if (cursor != null) {
                cursor.close()
            }

        }
        Log.d("FetchContact89", "fetchContacts: end")
        return listContacts

    }


    companion object {

        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}