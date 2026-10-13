package org.staymatch.pgdiscovery.mapper;

import org.springframework.stereotype.Component;
import org.staymatch.pgdiscovery.dto.ImageRequest;
import org.staymatch.pgdiscovery.dto.ImageResponse;
import org.staymatch.pgdiscovery.model.Image;

@Component
public class ImageMapper {

    public Image toEntity(ImageRequest request) {
        Image image = new Image();
        image.setImageUrl(request.getImageUrl());
        image.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        return image;
    }

    public ImageResponse toResponse(Image image) {
        ImageResponse response = new ImageResponse();
        response.setId(image.getId());
        response.setImageUrl(image.getImageUrl());
        response.setDisplayOrder(image.getDisplayOrder());
        return response;
    }
}

