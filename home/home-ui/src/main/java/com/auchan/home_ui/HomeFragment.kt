package com.auchan.home_ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.auchan.home_ui.databinding.HomeFragmentBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*


class HomeFragment : Fragment(R.layout.home_fragment) {

    private lateinit var binding: HomeFragmentBinding

    // Uri de l'image sélectionnée
    private var selectedImageUri: Uri? = null

    // Réseau social sélectionné
    private var selectedNetwork: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuration du Spinner avec la liste des réseaux sociaux
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.social_networks,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSocialNetwork.adapter = adapter

        // Ajout du listener sur le bouton pour afficher la liste des usernames
        binding.btnViewUsernames.setOnClickListener {
            fetchUsernamesFromFirestore()
        }

        // Gestionnaire d'événements pour la sélection du Spinner
        binding.spinnerSocialNetwork.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedNetwork = parentView.getItemAtPosition(position) as String
                Log.d("HomeFragment", "Réseau social sélectionné : $selectedNetwork")
            }


            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Pas besoin de gérer cela ici
            }
        }

        // Bouton pour ouvrir la galerie et sélectionner une image
        binding.btnAction.setOnClickListener {
            if (selectedNetwork != null) {
                openImagePicker(selectedNetwork!!)
            } else {
                Toast.makeText(requireContext(), "Veuillez sélectionner un réseau social", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Méthode pour récupérer les usernames depuis Firestore
    private fun fetchUsernamesFromFirestore() {
        val firestore = FirebaseFirestore.getInstance()

        // Récupération de tous les documents dans la collection "images"
        firestore.collection("images")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val usernameMap = mutableMapOf<String, Int>()

                // Parcourir les documents pour compter le nombre de rapports pour chaque utilisateur
                querySnapshot.documents.forEach { document ->
                    val username = document.getString("username")
                    if (username != null) {
                        usernameMap[username] = usernameMap.getOrDefault(username, 0) + 1
                    }
                }

                if (usernameMap.isNotEmpty()) {
                    // Appel de la méthode pour afficher la liste des usernames avec le nombre de rapports
                    showUsernamesDialog(usernameMap)
                } else {
                    Toast.makeText(requireContext(), "Aucun username trouvé", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Erreur lors de la récupération des usernames : ", e)
                Toast.makeText(requireContext(), "Erreur lors de la récupération des usernames", Toast.LENGTH_SHORT).show()
            }
    }


    // Méthode pour afficher les usernames dans une boîte de dialogue
// Méthode pour afficher les usernames dans une boîte de dialogue
    private fun showUsernamesDialog(usernameMap: Map<String, Int>) {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Liste des usernames")

        // Créer un EditText pour la recherche
        val searchEditText = android.widget.EditText(requireContext()).apply {
            hint = "Search by username"
            inputType = android.text.InputType.TYPE_CLASS_TEXT
        }

        // Ajouter l'EditText à la boîte de dialogue
        builder.setView(searchEditText)

        // Créer une liste formatée avec le username et le nombre de rapports
        val usernamesWithCounts = usernameMap.map { "${it.key} (${it.value} rapports)" }.toMutableList()

        // Créer un ArrayAdapter pour afficher la liste dans le dialogue
        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            usernamesWithCounts
        )
        builder.setAdapter(adapter) { dialog, which ->
            // Action possible quand un username est sélectionné
            val selectedUsername = usernamesWithCounts[which].split(" ")[0]
            Toast.makeText(requireContext(), "Username selected : $selectedUsername", Toast.LENGTH_SHORT).show()
        }

        // Fonction pour mettre à jour la liste des usernames en fonction du filtre de recherche
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val filter = s.toString().trim()
                val filteredUsernames = if (filter.startsWith("@")) {
                    // Filtrer la liste des usernames qui commencent par le caractère '@'
                    usernameMap.filter { it.key.startsWith(filter) }
                } else {
                    // Si le filtre ne commence pas par '@', afficher toute la liste
                    usernameMap
                }

                // Mettre à jour l'adaptateur avec la liste filtrée
                val filteredList = filteredUsernames.map { "${it.key} (${it.value} rapports)" }
                adapter.clear()
                adapter.addAll(filteredList)
                adapter.notifyDataSetChanged()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Pas besoin de gérer cela ici
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Pas besoin de gérer cela ici
            }
        })

        builder.setPositiveButton("Fermer", null)
        builder.show()
    }

    // Méthode pour ouvrir la galerie et sélectionner une image
    private fun openImagePicker(network: String) {
        Log.d("HomeFragment", "Ouverture du sélecteur d'image pour $network")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/webp")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    // Code de retour pour récupérer l'image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            selectedImageUri = data?.data
            Log.d("HomeFragment", "Image sélectionnée : $selectedImageUri")
            selectedImageUri?.let {
                if (isFileValid(it)) {
                    binding.selectedImageView.setImageURI(it)
                    binding.selectedImageView.visibility = View.VISIBLE
                    openImageDetailsDialog()
                } else {
                    Toast.makeText(requireContext(), "Le fichier sélectionné est vide ou invalide", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(requireContext(), "Échec de la sélection de l'image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Vérifie si le fichier est valide
    private fun isFileValid(uri: Uri): Boolean {
        val fileDescriptor = requireContext().contentResolver.openFileDescriptor(uri, "r")
        return fileDescriptor?.statSize ?: -1 > 0
    }

    // Fonction pour ouvrir la modale de détails de l'image
    private fun openImageDetailsDialog() {
        val dialog = ImageDetailsDialogFragment()
        dialog.setOnConfirmListener { username, description ->
            Log.d("HomeFragment", "Appel de resizeImage avec l'URI : $selectedImageUri")
            val resizedImage = selectedImageUri?.let { resizeImage(it) }
            resizedImage?.let { bitmap ->
                uploadImageToStorage(bitmap, username, description)
            }
        }
        dialog.show(parentFragmentManager, "ImageDetailsDialogFragment")
    }

    // Méthode pour redimensionner l'image sélectionnée
    private fun resizeImage(imageUri: Uri): Bitmap? {
        return try {
            Log.d("HomeFragment", "Redimensionnement de l'image")
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
            }

            // Redimensionner l'image à 800x800 pixels
            Bitmap.createScaledBitmap(bitmap, 800, 800, true)
        } catch (e: IOException) {
            Log.e("HomeFragment", "Erreur lors du redimensionnement de l'image: ", e)
            null
        } catch (e: UnsupportedOperationException) {
            Log.e("HomeFragment", "Le décodage de l'image a échoué en raison d'un format non supporté: ", e)
            Toast.makeText(requireContext(), "Format d'image non supporté", Toast.LENGTH_SHORT).show()
            null
        }
    }



    // Méthode pour uploader l'image redimensionnée sur Firebase Storage
    private fun uploadImageToStorage(bitmap: Bitmap, username: String, description: String) {
        Log.d("HomeFragment", "Début de l'upload de l'image redimensionnée")

        // Afficher le ProgressBar
        binding.progressBar.visibility = View.VISIBLE

        val storageReference: StorageReference = FirebaseStorage.getInstance().reference
        val imageName = UUID.randomUUID().toString()
        val imageRef = storageReference.child("images/$imageName")

        // Convertir le bitmap en bytes
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val imageData = baos.toByteArray()

        imageRef.putBytes(imageData)
            .addOnSuccessListener { taskSnapshot ->
                Log.d("HomeFragment", "Image redimensionnée uploadée avec succès")
                // Récupérer l'URL de l'image après l'upload
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("HomeFragment", "Image URL: $uri")
                    // Sauvegarder l'image dans Firestore avec son URL publique
                    saveImageToFirestore(uri.toString(), username, description)
                }
            }
            .addOnFailureListener { e ->
                Log.e("HomeFragment", "Erreur lors de l'upload de l'image: ", e)
                Toast.makeText(requireContext(), "Erreur lors de l'upload de l'image", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE // Cacher le ProgressBar en cas d'échec
            }
    }

    // Méthode pour enregistrer les informations de l'image dans Firestore
    private fun saveImageToFirestore(imageUrl: String, username: String, description: String) {
        Log.d("HomeFragment", "Enregistrement de l'image dans Firestore avec URL : $imageUrl")

        val firestore = FirebaseFirestore.getInstance()
        val imageData = hashMapOf(
            "imageUrl" to imageUrl,
            "network" to selectedNetwork,
            "username" to username,
            "description" to description,
            "uploadedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
            "safe" to true  // Champ "safe" qui sera mis à jour plus tard
        )

        firestore.collection("images")
            .add(imageData)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "Image ajoutée dans Firestore avec ID : ${documentReference.id}")

                // Simuler un délai pour la récupération du champ "safe"
                binding.progressBar.postDelayed({
                    // Appeler la fonction pour vérifier le statut "safe"
                    checkImageSafety(documentReference.id)
                }, 10000) // 10 secondes
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Erreur lors de l'enregistrement dans Firestore : ", e)
                Toast.makeText(requireContext(), "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE // Cacher le ProgressBar en cas d'échec
            }
    }

    // Méthode pour vérifier le statut "safe" de l'image après enregistrement
    private fun checkImageSafety(documentId: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("images").document(documentId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val isSafe = document.getBoolean("safe") ?: false
                    Log.d("Firestore", "Statut safe : $isSafe")

                    // Cacher le ProgressBar une fois la récupération terminée
                    binding.progressBar.visibility = View.GONE

                    // Afficher un message en fonction du statut "safe"
                    if (isSafe) {
                        Toast.makeText(requireContext(), "L'image est sûre !", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "L'image n'est pas sûre.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Erreur lors de la vérification du statut safe : ", e)
                binding.progressBar.visibility = View.GONE // Cacher le ProgressBar en cas d'échec
            }
    }

    // Constante pour le code de sélection d'image
    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}