package com.fuchuang.A33.DTO;

import com.fuchuang.A33.entity.Position;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemGroupPositionDTO {
    HashMap<String, Integer> PositionMap ;
    ArrayList<String> positions ;
}
