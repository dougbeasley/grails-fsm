import plugin.test.*

import grails.test.mixin.integration.Integration
import spock.lang.*

/**
* Cases for several actions attached to different transitions from
* same state with different when conditions attached.
*
*/
@Integration
class FsmMultipleActionsTests extends Specification {

  def "test first action"() {

    setup:
    def foo = new FsmMultipleActions()

    when:
    foo.hasErrors = false

    then:
    !foo.action1Called
    !foo.action2Called
    foo.status =='loaded'

    when:
    FsmMultipleActions.withNewSession {
      foo.fire_status('validate')
    }
    
    then:
    foo.status == 'validated'
    !foo.action1Called
    foo.action2Called

  }

  void testSecondAction() {
    def foo = new FsmMultipleActions()
    foo.hasErrors = true
    assert !foo.action1Called
    assert !foo.action2Called
    assert foo.status == 'loaded'

    foo.fire_status('validate')
    assert foo.status == 'in_error'
    assert foo.action1Called
    assert !foo.action2Called

  }

  void testAnyFromTransition() {

  }
}
