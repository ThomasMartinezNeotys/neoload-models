package com.neotys.neoload.model.v3.project;

import java.util.List;
import java.util.stream.Stream;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.neotys.neoload.model.v3.validation.constraints.RequiredCheck;
import com.neotys.neoload.model.v3.validation.groups.NeoLoad;

public interface ContainerElement extends ShareableElement {
	public static final String DO = "do";

	@JsonProperty(DO)
	@RequiredCheck(groups={NeoLoad.class})
	@Valid
	List<Element> getElements();
	
    @Override
    default Stream<Element> flattened() {
        return Stream.concat(Stream.of(this), getElements().stream().flatMap(Element::flattened));
    }
}