package com.neotys.neoload.model.readers.loadrunner.method;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.neotys.neoload.model.listener.TestEventListener;
import com.neotys.neoload.model.parsers.CPP14Parser.MethodcallContext;
import com.neotys.neoload.model.readers.loadrunner.ImmutableMethodCall;
import com.neotys.neoload.model.readers.loadrunner.LoadRunnerReader;
import com.neotys.neoload.model.readers.loadrunner.LoadRunnerVUVisitor;
import com.neotys.neoload.model.repository.CustomAction;
import com.neotys.neoload.model.repository.CustomActionParameter.Type;
import com.neotys.neoload.model.repository.ImmutableCustomAction;
import com.neotys.neoload.model.repository.ImmutableCustomActionParameter;

public class CustomActionMethodTest {

	private static final LoadRunnerReader LOAD_RUNNER_READER = new LoadRunnerReader(new TestEventListener(), "", "");
	private static final LoadRunnerVUVisitor LOAD_RUNNER_VISITOR = new LoadRunnerVUVisitor(LOAD_RUNNER_READER, "{", "}", "");
	private static final MethodcallContext METHOD_CALL_CONTEXT = new MethodcallContext(null, 0);
	private static final String CONNECTION_STRING = "   /SAP_CODEPAGE=1100   /FULLMENU SNC_PARTNERNAME=\"\" SNC_QOP=-1 /H/${SAP_IP_ADDRESS}/S/3217 /UPDOWNLOAD_CP=2";

	@Test
	public void test_sapgui_open_connection_ex() {
		final CustomAction actualCustomAction = (CustomAction) (new CustomActionMethod()).getElement(LOAD_RUNNER_VISITOR,
				ImmutableMethodCall.builder()
					.name("sapgui_open_connection_ex")
					.addParameters("\"" + CONNECTION_STRING + "\"")
					.addParameters("\"\"")
					.addParameters("\"con[0]\"")
					.build()
				, METHOD_CALL_CONTEXT);
	
		final CustomAction expectedCustomAction = ImmutableCustomAction.builder()
				.name("Connect")
				.type("SapConnect")
				.isHit(true)
				.parameters(ImmutableList.of(ImmutableCustomActionParameter.builder()
						.name("connectionString")
						.value("arg0")// TODO seb CONNECTION_STRING
						.type(Type.TEXT)
						.build()))
				.build();
		assertEquals(expectedCustomAction, actualCustomAction);
	}
}
