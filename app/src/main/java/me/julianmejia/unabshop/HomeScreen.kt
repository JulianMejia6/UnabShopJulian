package me.julianmejia.unabshop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onClickLogout: () -> Unit = {}) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    // Estados para el formulario
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precioTexto by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    // Estado para la lista de productos
    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }

    // Escuchar cambios en Firestore (realtime)
    DisposableEffect(Unit) {
        val registration = db.collection("productos")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    mensaje = "Error al cargar productos: ${e.message}"
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    productos = snapshot.documents.map { doc ->
                        Producto(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            precio = doc.getDouble("precio") ?: 0.0
                        )
                    }
                }
            }

        onDispose {
            registration.remove()
        }
    }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        "Unab Shop",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Notifications, "Notificaciones")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.ShoppingCart, "Carrito")
                    }
                    IconButton(onClick = {
                        auth.signOut()
                        onClickLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFFFF9900),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = { }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            // Parte superior: info usuario y botón cerrar sesión
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("HOME SCREEN", fontSize = 30.sp)

                if (user != null) {
                    Text(user.email.toString())
                } else {
                    Text("No hay usuario")
                }

                Button(
                    onClick = {
                        auth.signOut()
                        onClickLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9900))
                ) {
                    Text("Cerrar sesión")
                }
            }

            // Formulario + lista
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Agregar producto",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = Color(0xFF333333),
                    unfocusedLabelColor = Color(0xFF555555),
                    focusedBorderColor = Color(0xFFFF9900),
                    unfocusedBorderColor = Color(0xFF888888),
                    cursorColor = Color(0xFFFF9900)
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = precioTexto,
                    onValueChange = { precioTexto = it },
                    label = { Text("Precio") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                Button(
                    onClick = {
                        val precio = precioTexto.toDoubleOrNull()
                        if (nombre.isBlank() || descripcion.isBlank() || precio == null) {
                            mensaje = "Por favor completa todos los campos y usa un precio válido."
                        } else {
                            val producto = Producto(
                                nombre = nombre,
                                descripcion = descripcion,
                                precio = precio
                            )

                            db.collection("productos")
                                .add(producto)
                                .addOnSuccessListener {
                                    mensaje = "Producto guardado con éxito ✅"
                                    nombre = ""
                                    descripcion = ""
                                    precioTexto = ""
                                }
                                .addOnFailureListener { e ->
                                    mensaje = "Error al guardar: ${e.message}"
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9900))
                ) {
                    Text("Guardar producto")
                }

                if (mensaje.isNotBlank()) {
                    Text(
                        text = mensaje,
                        modifier = Modifier.padding(top = 8.dp),
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = "Productos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = 16.dp, bottom = 8.dp)
                )

                // Lista de productos (equivalente a RecyclerView)
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(productos) { producto ->
                        ProductoItem(
                            producto = producto,
                            onDelete = { prod ->
                                val id = prod.id
                                if (id == null) {
                                    mensaje = "No se pudo eliminar: id nulo"
                                } else {
                                    db.collection("productos")
                                        .document(id)
                                        .delete()
                                        .addOnSuccessListener {
                                            mensaje = "Producto eliminado ✅"
                                        }
                                        .addOnFailureListener { e ->
                                            mensaje = "Error al eliminar: ${e.message}"
                                        }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductoItem(
    producto: Producto,
    onDelete: (Producto) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            // Fondo más oscuro pero aún claro y limpio
            containerColor = Color(0xFFFFF3E0) // naranja muy suave
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = producto.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF222222)
                )
                Text(
                    text = producto.descripcion,
                    color = Color(0xFF444444)
                )
                Text(
                    text = "Precio: $${producto.precio}",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF222222)
                )
            }

            IconButton(
                onClick = { onDelete(producto) }
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar producto",
                    tint = Color(0xFFD32F2F) // rojo más oscuro y elegante
                )
            }
        }
    }
}
