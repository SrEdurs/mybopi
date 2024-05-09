package es.mybopi.controller;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import es.mybopi.model.Producto;
import es.mybopi.model.Usuario;
import es.mybopi.service.ProductoService;
import es.mybopi.service.UploadFileService;
import es.mybopi.service.UsuarioService;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private UploadFileService upload;
    
    @Autowired
    private ProductoService productoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/lista")
    public String detalles(Model model){
        model.addAttribute("inventario", productoService.findAll());
        return "productos/detalles";
    }

    //Pantalla crear productos
    @GetMapping("/crear")
    public String crearProducto(){
        return "productos/crear";
    }

    //Guardar un nuevo producto
    @PostMapping("/guardar")
    public String guardar(Producto producto, @RequestParam("img1") MultipartFile file, @RequestParam("img2") MultipartFile file2, @RequestParam("img3") MultipartFile file3) throws IOException{

        //Usuario
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Optional<Usuario> user = usuarioService.findByEmail(name);

        if(user.isPresent()){
            Usuario u = user.get();
            producto.setUsuario(u);

            //Fecha
            Date date = new Date();
            producto.setFecha(date);

            if(producto.getId() == null){ //Creando un producto
                String nombreImagen = upload.saveImage(file);
                producto.setPortada(nombreImagen);

                String nombreImagen2 = upload.saveImage(file2);
                producto.setImagen1(nombreImagen2);

                String nombreImagen3 = upload.saveImage(file3);
                producto.setImagen2(nombreImagen3);
            }

            productoService.save(producto);
            return "redirect:/productos/lista";
        } else{
            return "redirect:/";
        }

        
    }

    //Pantalla editar producto
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model){
        Producto producto = new Producto();
        Optional<Producto> optionalProducto = productoService.findById(id);

        producto = optionalProducto.get();

        model.addAttribute("producto", producto);

        return "productos/editar";
    }

    @PostMapping("/actualizar")
    public String actualizar(Producto producto, @RequestParam("img1") MultipartFile portada, @RequestParam("img2") MultipartFile imagen2, @RequestParam("img3") MultipartFile imagen3) throws IOException{

        Producto p = new Producto();
        p = productoService.findById(producto.getId()).get();

        //Fecha
        Date date = new Date();
        producto.setFecha(date);

        if(portada.isEmpty()){
            
            producto.setPortada(p.getPortada());
        } else { //Editando cambiando la imagen

            if(!p.getPortada().equals("default.jpg")){ //Si hay una portada por defecto
            upload.deleteImage(p.getPortada());
            }
            String nombreImagen = upload.saveImage(portada);
            producto.setPortada(nombreImagen);
        }

        //Imagen 1
        if(imagen2.isEmpty()){
            
            producto.setImagen1(p.getImagen1());
        } else { //Editando cambiando la imagen

            if(!p.getImagen1().equals("default.jpg")){ //Si hay una imagen por defecto
            upload.deleteImage(p.getImagen1());
            }
            String nombreImagen = upload.saveImage(imagen2);
            producto.setImagen1(nombreImagen);
        }

        //Imagen 2
        if(imagen3.isEmpty()){
            
            producto.setImagen2(p.getImagen2());
        } else { //Editando cambiando la imagen

            if(!p.getImagen2().equals("default.jpg")){ //Si hay una imagen por defecto
            upload.deleteImage(p.getImagen2());
            }
            String nombreImagen = upload.saveImage(imagen3);
            producto.setImagen2(nombreImagen);
        }

        producto.setUsuario(p.getUsuario());
        productoService.update(producto);

        return "redirect:/productos/lista";

    }


    //Acción de borrar un producto
    @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Integer id) throws IOException{
        Producto p = new Producto();
        p = productoService.findById(id).get();

        //Si la portada es por defecto no la borramos
        if(!p.getPortada().equals("default.jpg")){ 
        
            upload.deleteImage(p.getPortada());
        }

        //Si la imagen 1 es por defecto no la borramos
        if(!p.getImagen1().equals("default.jpg")){ 
        
            upload.deleteImage(p.getImagen1());
        }

        //Si la imagen 2 es por defecto no la borramos
        if(!p.getImagen2().equals("default.jpg")){ 
        
            upload.deleteImage(p.getImagen2());
        }

        
        
        productoService.deleteById(id);
        return "redirect:/productos/lista";

    }




}
