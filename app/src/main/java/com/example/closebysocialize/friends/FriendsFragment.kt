import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.closebysocialize.R
import com.example.closebysocialize.dataClass.Friend
import com.example.closebysocialize.friends.AddFriendFragment
import com.example.closebysocialize.utils.FirestoreUtils
import com.example.closebysocialize.utils.FragmentUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsFragment : Fragment(), FriendsAdapter.FriendClickListener {
    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    // comment git
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val floatingActionButton: FloatingActionButton = view.findViewById(R.id.addFriendsFloatingActionButton)
        floatingActionButton.setOnClickListener {
            floatingActionButton.animate().scaleX(0.7f).scaleY(0.7f).setDuration(200).withEndAction {
                floatingActionButton.animate().scaleX(1f).scaleY(1f).setDuration(200).withEndAction {
                    FragmentUtils.switchFragment(
                        activity = requireActivity() as AppCompatActivity,
                        containerId = R.id.fragment_container,
                        fragmentClass = AddFriendFragment::class.java,
                    )
                }
            }
        }

        friendsRecyclerView = view.findViewById(R.id.friendsRecyclerView)
        friendsAdapter = FriendsAdapter(requireContext(), listOf())
        friendsAdapter.listener = this
        friendsRecyclerView.adapter = friendsAdapter
        friendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadFriends()
    }


    override fun onResume() {
        super.onResume()
        loadFriends()
    }

    private fun loadFriends() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirestoreUtils.loadFriends(
            userId = userId,
            onSuccess = { friendsList ->
                friendsAdapter.updateData(friendsList)
            },
            onFailure = { exception ->
                Log.e("FriendsFragment", "Error loading friends: ", exception)
            }
        )
    }



    override fun onMessageClick(friend: Friend) {
    }
    override fun onFriendClick(friend: Friend) {
    }
    override fun onBinClick(friend: Friend) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val friendDocumentPath = "users/$userId/friends/${friend.id}"
        FirebaseFirestore.getInstance().document(friendDocumentPath).delete()
            .addOnSuccessListener {
                Log.d("FriendsFragment", "Friend successfully deleted: ${friend.id}")
                loadFriends()
            }
            .addOnFailureListener { e ->
                Log.e("FriendsFragment", "Error deleting friend: ", e)
            }
    }

}
