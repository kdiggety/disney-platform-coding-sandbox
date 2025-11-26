package interview;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlaceholderTest {
  @Test
  void hello_handles_null_and_blank() {
    assertEquals("hello", Placeholder.hello(null));
    assertEquals("hello", Placeholder.hello("   "));
  }

  @Test
  void hello_trims_name() {
    assertEquals("hello Ken", Placeholder.hello("  Ken  "));
  }
}
