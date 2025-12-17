package com.foodweb.foodapi.Implementation;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.foodweb.foodapi.entity.CartEntity;
import com.foodweb.foodapi.entity.FoodEntity;
import com.foodweb.foodapi.repository.CartRepository;
import com.foodweb.foodapi.repository.FoodRepository;
import com.foodweb.foodapi.request.FoodRequest;
import com.foodweb.foodapi.response.FoodResponse;
import com.foodweb.foodapi.service.FoodService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FoodServiceImpl implements FoodService {

    private final Cloudinary cloudinary;
    private final FoodRepository foodRepository;
    private final CartRepository cartRepository;

    public FoodServiceImpl(Cloudinary cloudinary, FoodRepository foodRepository, CartRepository cartRepository) {
        this.cloudinary = cloudinary;
        this.foodRepository = foodRepository;
        this.cartRepository = cartRepository;
    }

    @Override
    public String uploadFile(MultipartFile file) {
        // Extract file extension
        String fileExtension = getFileExtension(file.getOriginalFilename());
        // Create unique key (like S3 key)
        String publicId = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", "uploads/",          // Optional folder in your Cloudinary
                            "resource_type", "auto",       // Automatically detects file type
                            "access_mode", "public"        // Publicly accessible
                    )
            );

            // Get secure URL
            String uploadedUrl = uploadResult.get("secure_url").toString();

            if (uploadedUrl != null && !uploadedUrl.isEmpty()) {
                return uploadedUrl;  // same logic as returning s3 URL
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed: URL missing.");
            }

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "file";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    @Override
    public FoodResponse addFood(FoodRequest request, MultipartFile file) {
        FoodEntity newFoodEntity = convertToEntity(request);
        newFoodEntity.setActive(true);
        String imageUrl = uploadFile(file);
        newFoodEntity.setImageUrl(imageUrl);
        newFoodEntity = foodRepository.save(newFoodEntity);
        return convertToResponse(newFoodEntity);
    }


    private FoodEntity convertToEntity(FoodRequest request){
        return FoodEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .build();
    }

    private FoodResponse convertToResponse(FoodEntity entity){
        return FoodResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .price(entity.getPrice())
                .imageUrl(entity.getImageUrl())
                .build();
    }

//    @Override
//    public List<FoodResponse> readFoods() {
//        List<FoodEntity> dbEntries = foodRepository.findAll();
//        return dbEntries.stream().map(object -> convertToResponse(object)).collect(Collectors.toList());
//    }
@Override
public List<FoodResponse> readFoods() {
    List<FoodEntity> dbEntries = foodRepository.findByActiveTrue();
    return dbEntries.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
}


    @Override
    public FoodResponse readFood(Long id) {
        FoodEntity food = foodRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Food not found with id: " + id));
        return convertToResponse(food);
    }

    @Override
    public boolean deleteFile(String publicId) {
        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return "ok".equals(result.get("result"));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

//    @Override
//    public void deleteFood(Long id) {
//        FoodResponse response = readFood(id);
//        String publicId = extractPublicId(response.getImageUrl());
//        if (publicId == null) {
//            throw new RuntimeException("Failed to extract publicId from URL: " + response.getImageUrl());
//        }
//
//        boolean isFileDeleted = deleteFile(publicId);
//        if (isFileDeleted) {
//            foodRepository.deleteById(response.getId());
//        } else {
//            throw new RuntimeException("Failed to delete file from Cloudinary for ID: " + id);
//        }
//    }
@Override
@Transactional
public void deleteFood(Long id) {
    FoodEntity food = foodRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Food not found"));

    if (!food.isActive()) {
        throw new RuntimeException("Food already deleted");
    }

    // Delete image from cloud storage
    String publicId = extractPublicId(food.getImageUrl());
    if (publicId != null) {
        deleteFile(publicId);
    }

    // Soft delete
    food.setActive(false);
    foodRepository.save(food);
    // Remove this food from ALL carts
    List<CartEntity> carts = cartRepository.findAll();
    for (CartEntity cart : carts) {
        if (cart.getItems().containsKey(id)) {
            cart.getItems().remove(id);
            cartRepository.save(cart);
        }
    }
}


    private String extractPublicId(String imageUrl) {
        int uploadIndex = imageUrl.indexOf("/upload/");
        if (uploadIndex == -1) return null;
        String pathPart = imageUrl.substring(uploadIndex + 8);
        int slashAfterVersion = pathPart.indexOf("/");
        if (slashAfterVersion == -1) return null;
        String publicPath = pathPart.substring(slashAfterVersion + 1);
        return publicPath.replaceFirst("\\.[^.]+$", "");
    }


}



