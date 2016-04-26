package saulmm.avengers;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import saulmm.avengers.entities.Character;
import saulmm.avengers.rest.entities.RestCharacter;

public class GetCharactersUsecase extends Usecase<List<Character>> {
    public final static int DEFAULT_CHARACTERS_LIMIT = 20;
    private final Repository<Character> mCharacterRestRepository;
    private int mCharactersLimit = DEFAULT_CHARACTERS_LIMIT;
    private final CharacterDatasource mRepository;
    private int mCurrentOffset;

    private final Scheduler mUiThread;
    private final Scheduler mExecutorThread;

    @Inject public GetCharactersUsecase(
        CharacterDatasource repository,
        Repository<Character> characterRestRepository,
        @Named("ui_thread") Scheduler uiThread,
        @Named("executor_thread") Scheduler executorThread) {

        mRepository = repository;
        mUiThread = uiThread;
        mExecutorThread = executorThread;
        mCharacterRestRepository = characterRestRepository;
    }

    @Override
    public Observable<List<Character>> buildObservable() {
        return mRepository.getCharacters(mCurrentOffset)
            .observeOn(mUiThread)
            .subscribeOn(mExecutorThread)
            .doOnError(new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    mCurrentOffset -= mCharactersLimit;
                }
            });
    }

    @Override
    public Observable<List<Character>> execute() {
        increaseOffset();
        return super.execute();
    }

    public void increaseOffset() {
        mCurrentOffset += mCharactersLimit;
    }

    public int getCurrentOffset() {
        return mCurrentOffset;
    }
}
